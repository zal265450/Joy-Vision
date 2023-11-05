# 一路顺风！！！

# 介绍

抖鸭是一款web端短视频产品，虽然界面布局和抖音相似，但是我们团队只有前端哥们和后端小弟，没有UI，不过放心的代码是2周内日日夜夜的熬夜不睡觉写出来。

# 职责分工

## 前端

## 后端 - 胡展鸿

**技术选型：**

Jdk1.8+SpringBoot + MyBatis + MySql + Redis + 七牛云存储 + 七牛云审核

**主要职责：**

设计实现后端所有功能,功能在下面功能模块将展示

# 架构设计
![输入图片说明](image/%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.jpg%E6%8A%96%E9%B8%AD%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.jpg)

# 功能模块

暂时无法在飞书文档外展示此内容

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

### 推送视频

#### 兴趣推送

**用户**

用户在订阅分类后，可进行相似推送

**游客**

随机推送

##### 设计

**思路**

每个分类拥有默认标签，每个视频可拥有最多5个标签，用户订阅分类后，实际是订阅了分类后的标签，将标签概率分布，后续兴趣推送则获取用户的标签，进行随机概率获取推送视频。浏览停留时长,点赞,收藏行为将对对应的标签概率进行增减

**系统标签库**

数据结构：

Set  key:   system:stock:labelId    value: videoIds

**用户模型库**

数据结构:

Map key: user:model:userId    value : key labelId  value:概率

**流程**

1.用户订阅分类后取出分类后的默认标签，标签数/100 得到同等概率，存到用户模型库

2.用户在视频停留时长/点赞/收藏将对该视频下的所有标签拿到模型库中进行增长/缩减，每次增长后需要同等比缩小概率，**防止概率膨胀**

3.推送视频时获取用户的模型库，将其组装为数组，下标为labelId，随机数取数组长度，获取videoId，并和浏览记录去重，再根据用户性别标签获取视频，封装数据后返回，**这里可能会推送很少的视频，不要紧**

4.**前端是feed展示**，第一次请求时将获取所有，，后续每次请求则**根据阈值则提前推送**

暂时无法在飞书文档外展示此内容

#### 热门推送

数据结构：

Set : key: hot:video

##### 设计

每隔3个小时**切片快速分页扫描全表，**每个视频计算热度值后和系统配置表的热度值做比对，小于则放入热门视频

**推送：**

热门视频属于随机推送且下拉获取新视频，不需要分页，如果出现相似视频则说明是**数据量不够大**

#### 热度排行榜

根据视频的热度进行排行

**热度 = 权重 和时差的计算**

权重 = 点赞,浏览,分享,收藏*对应的占比例

时差：当前时间 - 视频发布时间的差值

##### 设计

A视频24小时内点赞到了1W，B视频1小时内点赞到了1W，则说明B视频热度更高

这里可以采用半衰期公式计算热度

总结可以理解为** 当前时间 - 视频发布时间 差值为x ，x越小y越大，x越大y越小 后\* 对应的权重 得到热度**

数据结构：

Zset： key：hot:rank  value: videoId socore: 热度  ttl: -1

每隔1个小时**切片快速分页扫描全表，**每个视频计算热度值后放入有界的小根堆 -> **TopK问题**

放入Redis后，只保留50个视频

#### 关注推送

推送关注人发送的视频 -> feed流

##### 设计

用户拥有**发件箱**和**收件箱**

**发件箱**

用户所发布的视频存储在发件箱

数据结构：

Zset key: out:follow:feed:userId  value: videoId  score:视频发布时间 TTL:-1

**收件箱**

存储用户关注人的视频

数据结构：

Zset key：in:follow:feed:userId value:videoId score:视频发布时间 TTL:5天

**流程**

1.用户发布视频后，将视频异步发送到发件箱

2.用户上线后异步获取关注流：

2.1关注流为空，则拉取关注人7天之内的视频

2.2不为空，则拉取收件箱最新视频的时间 - 当前时间内关注人的视频并存入收件箱

3.用户删除视频将异步删除发件箱视频，以及粉丝内的收件箱视频

4.用户拉取关注流根据滚动分页获取

推拉模式的选择是需要根据当前项目的数据体量决定的。当前项目体量不大，选择拉模式且设置ttl，过滤不活跃粉丝

#### 分类推送

根据分类随机推送视频，不需要分页，不需要去重，因数据量少

**一切的设计实现都要考虑当前项目的因素落地**

## 审核中台

说明:此处的审核可以是**审核中台**，但项目体量不大，因此嵌入式项目，不然就要进行RPC调用增加开发成本

内容可自定义审核比例放行拦截，例如：我们可以将违规的视频发行发布

### 设计

AuditService: 规定审核标准，规定入参返回值 <T,R>

AbstractAuditService: 封装统一逻辑 : 比较得分，获取消息，返回对应审核状态(策略模式)

ImageAuditService: 图片审核

TextAuditService: 内容审核

VideoAuditService: 视频审核

![img](https://qg9rv6a511.feishu.cn/space/api/box/stream/download/asynccode/?code=YWNiY2IyZmY3MzkzMDRlMmUzZTU0YTdmOWYyYzUwNjZfZXpTMGdIQUpYM0JZUFdhY3pBNEpHMnFDaXZ4cUNjc3JfVG9rZW46TkJiYmJKczhvb3JSenJ4cExEYWNYZ0Jxbk1lXzE2OTkxNjAzNDQ6MTY5OTE2Mzk0NF9WNA)

VideoPublishAuditServiceImpl: 发布视频审核逻辑

![img](https://qg9rv6a511.feishu.cn/space/api/box/stream/download/asynccode/?code=MGRiMjljY2I4ZjI3OWUxOTMzYzM3MjhlZmUzYWUyYTRfd3JCM2piN1dEd21MM2gwZ3U3ZjBDUjZrUzd0UDhqRDlfVG9rZW46QWhoZGJBRUN0b2Y5bWh4bXBlbmNNc0VWbmNjXzE2OTkxNjAzNDQ6MTY5OTE2Mzk0NF9WNA)
