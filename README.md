# routing-dynamic-session
동적 라우팅 멀티 데이터소스 구현

- 기존 맵 방식으로 구현된 dynamic-session과 다르게 별도로 routing 구현
- 공통 디비(CommonDB 등)로 부터 데이터베이스 정보를 읽어와 여러개의 월드 데이터소스를 동적 구현한다.
- 하위 DB 간 트랜젝션 처리 시 일관성을 가질 수 있도록 한다. (XA)
- ThreadLocal주입 Annotation Aspect 구현으로 수작업으로 Context 수정 안해도 되게 하고, @Mapper Interface로 Mapper 연결(singleton) 
- ChainedTransactionManager으로 해도 될 듯

---

### /api/root/databases
공통 디비에서 월드 조회. 다음과 같다.
```bash
{
  "result": [
    {
      "id": 1,
      "name": "world1"
    },
    {
      "id": 2,
      "name": "world2"
    }
  ],
  "status": "OK"
}
```

이후 해당 정보로 데이터소스를 동적으로 구현하는 기능을 가진다. 
### /api/users/{name}
- 해당 이름으로 name_1, _2, _3 유저를 모든 월드에 생성한다.
- TestError 로 입력 시 1번 월드 생성 도중 에러 발생, ErrorTest로 입력 시 2번 월드 생성 도중 에러 발생을 강제시킨다.

**XA가 적용된 Datasource와 TxManager 사용 시 에러 발생 시 두개의 DB 모두 롤백되어있는 것을 확인 할 수 있다.**

*XA가 적용되지 않은 일반 TxManager 및 DataSource 사용 시 해당하는 DB만 롤백된다. 또한 routeTxManger가 Datasource 선택 시 LookupKey가 없을 시
에러를 먼저 뱉기 때문에... Transaction 적용이 필요한 곳은 별도로 분리 해서 만들어야 한다. (소스상엔 XA 구현되어있음)*

---

### /api/users?worldId=1
### /api/users?worldId=2
- 월드 별 유저를 조회한다.

위의 두 endpoint로 접근하게 되는 빈은 같은 RouteDatasource, SqlSessionTemplate, SqlSesstionFactory, MybatisMapper, TransactionManager(여기서는 구현 안함)를 
사용함으로서 불필요하게 **동일한 역할을 하는 빈을 과다하게 로드할 필요가 없어진다.** (내부적으로 Datasource Bean은 DB수만큼 생김)

---

```bash
# /actuator/beans
"beans": {
        "userMapperImpl": {
          "aliases": [],
          "scope": "singleton",
          "type": "com.route.datasource.repository.UserMapperImpl$$EnhancerBySpringCGLIB$$dda52561",
          "resource": "file [C:\\Users\\seolm\\eclipse-workspace\\spring-route-datasource\\target\\classes\\com\\route\\datasource\\repository\\UserMapperImpl.class]",
          "dependencies": [
            "routingSessionTemplate"
          ]
        },
        ....
```
- 다음과 같이 여러개의 Datasource여도 한개 **Singleton Mapper 빈만 로드된다.**
- {world}_datasource + {world}_mapper 를 별도 구성할 필요가 없다. (~~world * mapper는..좀..~~)

---

## Datasource Mapping 구조
```bash
# /actuator/health
"db": {
      "status": "UP",
      "components": {
        "rootDataSource": {
          "status": "UP",
          "details": {
            "database": "H2",
            "validationQuery": "isValid()"
          }
        },
        "routingDataSource": {
          "status": "UP",
          "components": {
            "1": {
              "status": "UP",
              "details": {
                "database": "H2",
                "validationQuery": "isValid()"
              }
            },
            "2": {
              "status": "UP",
              "details": {
                "database": "H2",
                "validationQuery": "isValid()"
              }
            }
          }
        }
      }
```
- 라우트 데이터 소스 아래의 컴포넌트로 Map<[key],Datasource> 형태로 하위 구현되는 걸 확인 할 수 있다.

## routingSessionFactory의 TransactionFactory 교체

기본으로 **SpringManagedTransactionFactory** 을 사용하다보니 Sync된 한개의 Datasource만 계속 보는 현상이 있다.
인터넷을 보니 **ManagedTransactionFactory** 로 교체하면 된다고 하여 교체하였었는데, 커넥션을 재사용하지 않고
쿼리 하나하나 마다 계속 **connection->query->clos**e를 반복하고 있었다. 그래서 커넥션을 재사용하도록 **MultiDataSourceTransactionFactory** 를 구현해서 바꿨다.

