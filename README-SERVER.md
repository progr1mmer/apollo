# Apollo-Server
主干([Apollo（阿波罗）](https://github.com/ctripcorp/apollo))源码基础上开发的三合一版本，在简化部署流程、减少部署成本的情况下提供完整的功能。
* **使用**
  * 初始化数据库，脚本: [apolloserverdb.sql](https://github.com/progr1mmer/apollo/tree/master/apollo-server/sql/apolloserverdb.sql)
  * 启动此服务，访问: [http://localhost:8080/index.html](http://localhost:8080/index.html)
  * 客户端使用与主干无疑
* **注意事项**
  * 使用认证环境的时候请确保[com.ctrip.framework.apollo.portal.component.RetryableRestTemplate](https://github.com/progr1mmer/apollo/tree/master/apollo-server/src/main/com/ctrip/framework/apollo/portal/component/RetryableRestTemplate.java)拥有root权限账号的http-basic认证信息
  * 启动的时候会有一些报错信息，属于正常情况，请忽略
