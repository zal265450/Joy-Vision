import * as qiniu from 'qiniu-js'
import request from './request'

const Config = {
    qiniuOSS: {}
}

export const apiVideoGetToken = async() => await request.get("/video/token")

export const apiVideoUpload = async (file, callBack={next:()=>{},error:()=>{}, complete:()=>{}})=> {
    const videoToken = (await apiVideoGetToken()).data.data
    const observable = qiniu.upload(file, null, videoToken, {}, Config.qiniuOSS)
    const subscription = observable.subscribe(callBack.next, callBack.error, callBack.complete) // 这样传参形式也可以
    // subscription.unsubscribe() // 上传取消
    return subscription;
}

/**
 * 根据分类获取视频
 * @param {int} classfiyId 分类id
 */
export const apiVideoByClassfiy = (classfiyId)=>{
    if(classfiyId>0)
    return request.get(`/index/video/type/${classfiyId}`)
    return apiVideoByPush()
}

/**
 * 获取主页推送视频
 */
export const apiVideoByPush = ()=>{
    return request.get("/index/pushVideos")
}

/**
 * 发布视频/修改视频
 * @param {object} videoInfo 视频信息
 * @returns 成功
 */
export const apiVideoPush = (videoInfo)=>{
    videoInfo.auditStatus = 0
    return request.post("/video", videoInfo);
}

/**
 * 获取指定收藏夹的视频
 * @param {int} favoriteId 收藏夹id
 * @returns 成功
 */
export const apiGetVideoByFavoriteId = (favoriteId=0)=>{
    return request.get(`/video/favorites/${favoriteId}`)
}

/**
 * 获取用户自己的视频
 * @param {int} page 当前页 
 * @param {int} limit 条数
 * @returns 成功
 */
export const apiGetVideoByUser = (page=1, limit=5)=>{
    return request.get(`/video`, {
        params: {
            page,
            limit
        }
    })
}

/**
 * 根据视频标签推送相似视频
 * @param {Array<String>} labels 视频标签列表
 * @returns 
 */
export const apiGetVideoBySimilar = (labels)=>{
    return request.get(`/index/video/similar`, {
        params: {
            labels
        }
    })
}

/**
 * 点赞视频
 * @param {int} videoId 视频id
 * @returns 
 */
export const apiStarVideo = (videoId)=>{
    return request.post(`/video/star/${videoId}`)
}

/**
 * 获取浏览记录
 * @returns 
 */
export const apiGetHistoryVideo = ()=>{
    return request.post(`/video/history`)
}

/**
 * 搜索视频
 * @param {string} searchName 搜索参数,可能是标题,用户,YV
 * @param {int} page 当前页
 * @param {int} limit 条数
 * @returns 
 */
export const apiSearchVideo = (searchName, page=1, limit=10) =>{
    return request.get(`/index/search`, {
        params: {
            searchName,
            page,
            limit
        }
    })
}