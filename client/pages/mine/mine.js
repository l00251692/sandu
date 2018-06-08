// pages/mine/mine.js
import { getUserInfo, makePhoneCall } from '../../utils/util'
import { logout, getMineInfo } from '../../utils/api'

const app = getApp()
Page({
  data: { 
    loginInfo:null
  },
  onLoad:function(options){
    
  },
  onReady:function(){
    // 页面渲染完成
  },
  onShow:function(){
    var that = this
    getApp().getLoginInfo(loginInfo => {
      if (loginInfo != null && loginInfo.is_login) {
        that.setData({
          loginInfo: loginInfo,
          userInfo: loginInfo.userInfo
        })

        getMineInfo({
          success(data) {
            that.setData({
              campus_id: data.campus_id,
              count: 0
            })
          }
        })
      }
    })
  },
  onHide:function(){
    // 页面隐藏
  },
  onUnload:function(){
    // 页面关闭
  },
  onPhoneTap(e) {
    makePhoneCall(e.currentTarget.dataset.phone)
  },
  onLogout(e) {
    var that = this
    var {loginInfo: {phone}, loading} = this.data
    if(loading) {
      return
    }
    this.setData({
      loading: true
    })
    logout({
      phone,
      success(data) {
        app.setLoginInfo(data)
        that.setData({
          loginInfo: null,
          loading: false
        })
      }
    })
  },
  callback() {
    this.onLogin()
  },
  onLogin(){
    var { loginInfo} = this.data
    if (loginInfo == null)
    {
      var that = this
      getApp().getLoginInfo(loginInfo => {
        if (loginInfo != null && loginInfo.is_login) {
          that.setData({
            loginInfo: loginInfo,
            userInfo: loginInfo.userInfo
          })

          getMineInfo({
            success(data) {
              that.setData({
                campus_id: data.campus_id,
                count: data.unread_msg_count
              })
            }
          })
        }
      })
    }
  },
  onShareAppMessage() {
    return {
      title: '我的信息',
      path: '/pages/mine/mine'
    }
  }
})