import request from '../request'

/**
 * 获取用户信息
 * @param {Int} userId 用户Id 
 * @returns 
 */
export const apiGetUserInfo = (userId="")=>{
    return request.get(`/customer/getInfo/${userId}`)
}