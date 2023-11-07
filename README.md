

# 介绍

幸运日是一款短视频平台，拥有热度排行榜，热门视频，兴趣推送，关注推送，内容审核等功能


👍 [项目地址](http://luckjourney.liuscraft.top/#/)

[demo视频地址1：http://s36d82b8z.hn-bkt.clouddn.com/demo/幸运日项目展示.mp4](http://s36d82b8z.hn-bkt.clouddn.com/demo/%E5%B9%B8%E8%BF%90%E6%97%A5%E9%A1%B9%E7%9B%AE%E5%B1%95%E7%A4%BA.mp4)

[demo视频地址2：http://luckjourney.liuscraft.top/#/?play=4819](luckjourney.liuscraft.top/#/?play=4819)  首选


[demo视频地址3：链接：https://pan.baidu.com/s/1NCoawrvnpNSfmMRMbAaEqg 提取码：zjof](https://pan.baidu.com/s/1NCoawrvnpNSfmMRMbAaEqg)

 **注意：该项目技术选型，方案实现，是基于当前项目体量以及实际场景选择，避免过度设计，增加服务器成本，开发成本，运营成本。优化地方下面会提出** 

# 职责分工

 **前端 - 刘顺顺**

 负责设计实现前端所有模块的页面和交互 

 **技术选型：**

vite + vue3 + axios + pina + router + vuetifyUI + videojs + 七牛云sdk

 **后端 - 胡展鸿** 

负责设计实现后端所有模块

**技术选型：**

Jdk1.8 + SpringBoot + MyBatis + MySql + Redis + 七牛云存储 + 七牛云审核 + 七牛云转码 + 七牛云回源鉴权


# 使用说明

 **后端** 

1.将docs中sql文件导入  **注: 如果导入全部数据的sql文件请联系我** 

2.配置参数： MySql数据源,redis数据源,邮箱参数,七牛云参数,七牛云CNAME

3.配置七牛云回源鉴权相关参数

4.配置系统配置表白名单(放行资源),回源鉴权开关

后台管理地址:http://localhost:8882/page/login.html

 **前端** 

 **本地启动** 

1.进入front-end文件夹

2.vite.config.js 更改proxy服务为后端的正确ip:port/luckyjourney

3.执行 `yarn` 下载依赖 

4.执行 `yarn dev` 运行dev项目

5.访问: http://127.0.0.1:5378/#/ 可查看前端项目。

 **部署** 

> 准备 Node.js v16.15.1 环境

1.进入front-end文件夹

3.执行 `yarn` 下载依赖 

4.执行 `yarn build` 构建项目

5.搭建nginx环境：nginx 1.22

6.将构建好的前端项目部署到nginx

7.配置反向代理:http://127.0.0.1:8882/luckyjourney/ 更改为自己的后端服务地址
```
#PROXY-START/

location ^~ /api/
{
    proxy_pass http://127.0.0.1:8882/luckyjourney/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header REMOTE-HOST $remote_addr;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection $connection_upgrade;
    proxy_http_version 1.1;
    # proxy_hide_header Upgrade;

    add_header X-Cache $upstream_cache_status;
    #Set Nginx Cache

    set $static_filegKiLSCbO 0;
    if ( $uri ~* "\.(gif|png|jpg|css|js|woff|woff2)$" )
    {
        set $static_filegKiLSCbO 1;
        expires 1m;
    }
    if ( $static_filegKiLSCbO = 0 )
    {
        add_header Cache-Control no-cache;
    }
}
#PROXY-END/
```

# 项目截图


登录/注册

![输入图片说明](image/%E7%99%BB%E5%BD%95/%E6%B3%A8%E5%86%8C.png)


首页

![输入图片说明](image/%E9%A6%96%E9%A1%B5.png)

视频播放（可滚动播放）,右侧是视频详情以及 **相似视频推送** 

![输入图片说明](image/%E6%92%AD%E6%94%BE%E8%A7%86%E9%A2%91.png)


创作中心

![输入图片说明](image/%E5%88%9B%E4%BD%9C%E4%B8%AD%E5%BF%83.png)

# 架构设计

![输入图片说明](image/%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.jpg)

# 功能模块

![输入图片说明](image/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97.png)

## 用户

### 关注/粉丝

用户可对TA人进行关注/粉丝，关注后即可在关注推送TA人的视频，取关后取消推送

### 用户收藏夹

用户可对视频进行收藏管理

**注：用户注册后生成默认收藏夹，不可删除**

### 浏览记录

用户所刷的视频将保存在此为5天

### 订阅分类

用户可订阅感兴趣的分类，后续在推荐页中可被推荐更多相似视频

## 视频

### 上传视频

![输入图片说明](image/%E4%B8%8A%E4%BC%A0%E8%A7%86%E9%A2%91.png)


### 播放视频

视频存储在云端,需要被保护,用到了七牛云的回源鉴权

 **流程** 

1.获取资源请求服务器

2.服务器判断当前请求referer是否包含在系统白名单内

3.1.不包含 403 END

3.2.包含

4.生成UUID TTL:8秒 放入caffeine中并且拼接url

5.重定向七牛云资源

6.七牛云转发鉴权服务

7.鉴权服务校验是否合法

![输入图片说明](image/%E8%8E%B7%E5%8F%96%E8%B5%84%E6%BA%90%E6%B5%81%E7%A8%8B%E5%9B%BE.png)


### 兴趣推送

**用户**

用户在订阅分类后，可进行相似推送

**游客**

随机分类推送

 **设计** 


每个分类拥有默认标签，每个视频可拥有最多5个标签，用户订阅分类后，实际是订阅了分类后的标签，将标签概率分布，后续兴趣推送则获取用户的标签，进行随机概率获取推送视频。浏览停留时长,点赞,收藏行为将对对应的标签概率进行增减

**系统标签库**

数据结构：


```
Set ttl: -1

key: system:stock: + 标签id

value: 视频ids
```


**用户模型库**

数据结构:


```
Map ttl: -1

key: user:model: + 用户id
 
value : 

key: 标签id

value: 概率
```


**流程**

1.用户订阅分类后取出分类后的默认标签，标签数/100 得到同等概率，存到用户模型库

2.用户在 **视频停留时长/点赞/收藏** 将对该视频下的所有标签拿到 **模型库中进行增长/缩减** ，每次增长后需要同等比缩小概率，**防止概率膨胀**

3.推送视频时获取用户的模型库，将其组装为数组，下标为labelId，随机数取数组长度，获取videoId，并和浏览记录去重，再根据用户性别标签获取视频，封装数据后返回，**这里可能会推送很少的视频，不要紧**

4.**前端拉取推荐视频根据上一次推送视频集合的阈值拉取,例如集合扩容机制** 

推送流程图

![输入图片说明](image/%E5%85%B4%E8%B6%A3%E6%8E%A8%E9%80%81.png)

### 热门推送

数据结构：


```
Set ttl: 3天

key: hot:video

value: hotVideo
```


**设计** 

每隔3个小时 **切片快速分页扫描全表** ，每个视频计算热度值后和系统配置表的热度值做比对，小于则放入热门视频

**推送：**

热门视频属于随机推送且下拉获取新视频，不需要分页，如果出现相似视频则说明是**数据量不够大**

### 热度排行榜

根据视频的热度进行排行

 **设计** 

**热度 = 权重 和时差的计算**

权重 = 点赞,浏览,分享,收藏对应的占比例

时差：当前时间 - 视频发布时间的差值

A视频24小时内点赞到了1W，B视频1小时内点赞到了1W，则说明B视频热度更高

这里可以采用半衰期公式计算热度

总结可以理解为  **当前时间 - 视频发布时间 差值为x ，x越小y越大，x越大y越小 后 对应的权重 得到热度** 

数据结构：


```
Zset ttl: -1

key：hot:rank  

value: videoId 

socore: 热度  
```


每隔1个小时切片快速分页扫描全表，**每个视频计算热度值后放入有界的小根堆,遍历完成再放入Redis -> TopK问题**

### 关注推送

推送关注人发送的视频 -> feed流

 **设计** 

用户拥有**发件箱**和**收件箱**

**发件箱**

用户所发布的视频存储在发件箱

数据结构：


```
Zset ttl:-1

key: out:follow:feed: + 用户id

value: 视频id

Score: 视频发布时间 
```


**收件箱**

存储用户关注人的视频

数据结构：

```
Zset ttl:5天

key：in:follow:feed: + 用户id

value: 视频id

score: 视频发布时间 
```


**流程**

1.用户发布视频后，将视频异步发送到发件箱

2.用户上线后异步获取关注流：

  2.1关注流为空，则拉取关注人7天之内的视频

  2.2不为空，则拉取收件箱最新视频的时间 - 当前时间内关注人的视频并存入收件箱

3.用户删除视频将异步删除发件箱视频，以及粉丝内的收件箱视频

4.用户拉取关注流根据滚动分页获取

推拉模式的选择是需要根据当前项目的数据体量决定的。当前项目体量不大，选择拉模式且设置ttl，过滤不活跃粉丝


 **收件箱初始化**

![输入图片说明](image/%E6%94%B6%E4%BB%B6%E7%AE%B1%E5%88%9D%E5%A7%8B%E5%8C%96.png)


 **拉取关注视频**
  
![输入图片说明](image/%E6%8B%89%E5%8F%96%E6%94%B6%E4%BB%B6%E7%AE%B1%E8%A7%86%E9%A2%91.png)


### 分类推送

根据分类随机推送视频，不需要分页，不需要去重，因数据量少

**一切的设计实现都要考虑当前项目的因素落地**

## 审核中台

审核中台可自定义放行比例以及设置是否开启审核

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E5%AE%A1%E6%A0%B8%E7%95%8C%E9%9D%A2.png)

 **设计** 

AuditService: 规定审核标准，规定入参返回值 <T,R>

AbstractAuditService: 封装统一逻辑 : 比较得分，获取消息，返回对应审核状态(策略模式)

ImageAuditService: 图片审核

TextAuditService: 内容审核

VideoAuditService: 视频审核

![输入图片说明](image/%E5%AE%A1%E6%A0%B8%E4%B8%AD%E5%8F%B0%E8%AE%BE%E8%AE%A1.png)

VideoPublishAuditServiceImpl: 发布视频审核设计

![输入图片说明](image/%E9%A1%B9%E7%9B%AE%E5%AE%A1%E6%A0%B8%E8%90%BD%E5%9C%B0%E5%AE%9E%E7%8E%B0.png)

## 后台管理界面

### 权限模块

使用RBAC实现权限模块,超级管理员可自行分配角色

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E6%9D%83%E9%99%90%E7%95%8C%E9%9D%A2.png)

### 系统配置

系统配置中配置了审核力度、审核开关、热门视频热度限制、白名单

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E7%B3%BB%E7%BB%9F%E9%85%8D%E7%BD%AE.png)


### 视频模块

可对视频进行下架审核处理

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E8%A7%86%E9%A2%91.png)

### 分类模块

可管理首页的分类

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E5%88%86%E7%B1%BB.png)


## 改进优化

### 架构改进

当前项目为单体架构,为什么没考虑微服务如下:

1.个人认为无论是实际项目还是比赛,不以 **实际解决问题出发而引入某些技术就是在炫技** ,无非是增加了开发成本,运维成本等。例如：不考虑用户体量的场景下就做分库表,引入ES做搜索,MQ做异步解耦

架构改进如下:

 **1.视频服务** 



 **2.用户服务** 


 **3.评论服务** 

评论服务抽出来是考虑到后续产品会出 **动态** 功能,因此将评论服务抽出来
 

 **4.点赞收藏浏览分享服务** 

该服务考虑到后续可能会有对视频,动态,评论等有操作

 **5.审核中台** 


 **6.鉴权服务** 

用于对资源的保护

 **7.网关** 

路由请求转发


### @Async 改进 MQ

在项目中大量使用了线程解耦,实际引入MQ

### 分片存储视频

项目中Redis有一个分类库,用于存储所有的视频达到随机推送视频，且ttl为-1,项目中未做分片,会造成大key

 **分片设计** 

1.每个分类维护一个分片id,且限制分片id最大存储X条数据。 数据结构String key: 分类id  value: 自增id

2.系统启动时将分片id存储本地缓存

3.存储视频时,先判断对应分类中的数量是否达到限制

3.1.未达到 - 跳到4
3.2.达到限制  - 将本地缓存自增1,异步修改Redis对应分类id   跳到4

4.获取本地缓存对应分类id

5.取对应id内的随机数，达到分片获取数据。如果想避免数据倾斜(随机数很旧,获取视频不是最新),可指定具体id进行获取数据

### Feed流

当前项目中是以拉模式实现,用户上线后拉取内容且设置ttl。这里应该做成推拉模式，用户发布一个视频后，推送到活跃用户的收件箱

这里的设计是考虑了项目体量决定

### 对象存储

对象存储在项目中是将和资源相关暴露给了前端,实际该尽可能减少暴露

 **设计** 

1.设计文件表,用于管理所有的文件

2.视频表关联文件表, file_id = file_key

3.获取资源时根据file_id从文件表中查询file_key进行重定向

4.鉴权...

### 审核中台

在项目中审核的设计为嵌入式服务,应该将其抽出改为单独服务,并且提供更多的信息


### 分享

分享未做短链接,实际应该做短链接处理,存储视频信息,用户信息等
