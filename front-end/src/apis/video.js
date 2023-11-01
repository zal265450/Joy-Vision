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
 * @param {分类id} classfiyId 
 */
export const apiVideoByClassfiy = (classfiyId)=>{
    return request.get(`/index/video/type/${classfiyId}`)
}

/**
 * 获取主页推送视频
 */
export const apiVideoByPush = ()=>{
    return request.get("/index/pushVideos")
}

/**
 * 发布视频/修改视频
 * @param {视频信息} videoInfo 
 * @returns 成功
 */
export const apiVideoPush = (videoInfo)=>{
    videoInfo.auditStatus = 0
    return request.post("/video", videoInfo);
}