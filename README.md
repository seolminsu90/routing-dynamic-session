# routing-dynamic-session
동적 라우팅 멀티 데이터소스 구현

- 기존 맵 방식으로 구현된 dynamic-session과 다르게 별도로 routing 구현
- 공통 디비(CommonDB 등)로 부터 데이터베이스 정보를 읽어와 여러개의 월드 데이터소스를 동적 구현한다.

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
### /api/users?worldId=1
```bash
{
  "result": [
    {
      "id": 1,
      "name": "seolminsu"
    },
    {
      "id": 2,
      "name": "leemj"
    }
  ],
  "status": "OK"
}
```
### /api/users?worldId=2
```bash
{
  "result": [
    {
      "id": 1,
      "name": "seolminsu"
    },
    {
      "id": 2,
      "name": "hannachu"
    },
    {
      "id": 3,
      "name": "lucy"
    }
  ],
  "status": "OK"
}
```
위의 두 endpoint로 접근하게 되는 빈은 같은 RouteDatasource, SqlSessionTemplate, SqlSesstionFactory, MybatisMapper, TransactionManager(여기서는 구현 안함)를 
사용함으로서 불필요하게 **동일한 역할을 하는 빈을 과다하게 로드할 필요가 없어진다.** (내부적으로 Datasource Bean은 DB수만큼 생김)

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
