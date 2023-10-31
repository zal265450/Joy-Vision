import request from './request'

/**
 * 获取所有视频分类
 * @returns 分类
 */
export const apiClassifyGetAll = ()=>{
    return request.get("/index/types")
}