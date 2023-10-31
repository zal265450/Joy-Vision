import * as qiniu from 'qiniu-js'
import request from './request'

const Config = {
    qiniuOSS: {}
}

export const apiVideoGetToken = async() => await request.get("/video/token")

export const apiVideoUpload = (file, callBack={next:()=>{},error:()=>{}, complete:()=>{}})=> {
    const videoToken = apiVideoGetToken()
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