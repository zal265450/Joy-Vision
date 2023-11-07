

# 介绍

抖鸭是一款短视频平台，拥有热度排行榜，热门视频，兴趣推送，关注推送，内容审核等功能

 **注意：该项目技术选型，方案实现，是基于当前项目体量以及实际场景选择，避免过度设计，增加服务器成本，开发成本，运营成本。优化地方下面会提出** 

# 职责分工

 **前端** 

 **后端** 

**技术选型：**

Jdk1.8 + SpringBoot + MyBatis + MySql + Redis + 七牛云存储 + 七牛云审核 + 七牛云转码 + 七牛云回源鉴权


# 使用说明

 **后端** 
1.将docs中sql文件导入
2.配置参数： MySql数据源,redis数据源,邮箱参数,七牛云参数,七牛云CNAME
3.配置七牛云回源鉴权相关参数

后台管理地址:http://localhost:8882/page/login.html

# 架构设计
![输入图片说明](image/%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.jpg)

# 功能模块

![输入图片说明](image/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97.jpg.png)

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

4. **前端拉取推荐视频根据上一次推送视频集合的阈值拉取,例如集合扩容机制** 

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


每隔1个小时**切片快速分页扫描全表，**每个视频计算热度值后放入有界的小根堆,遍历完成再放入Redis -> TopK问题**

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

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E6%9D%83%E9%99%90%E7%95%8C%E9%9D%A2.png)

### 系统配置

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E7%B3%BB%E7%BB%9F%E9%85%8D%E7%BD%AE.png)

### 视频模块

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E8%A7%86%E9%A2%91.png)

### 分类模块

![输入图片说明](image/%E5%90%8E%E5%8F%B0%E5%88%86%E7%B1%BB.png)


## 改进优化