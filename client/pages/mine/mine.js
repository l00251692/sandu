// pages/mine/mine.js
import { getUserInfo, makePhoneCall, alert } from '../../utils/util'
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
    //用户授权则登录，否则等用户点击授权
    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userInfo'])
        {
          getApp().getLoginInfo(loginInfo => {
            if (loginInfo != null && loginInfo.is_login) {
              that.setData({
                loginInfo: loginInfo,
                userInfo: loginInfo.userInfo
              })

              getMineInfo({
                success(data) {
                  console.log("getMineInfo :")
                  console.log(data)
                  if (data.phone != null && data.phone.length > 0) {
                    that.setData({
                      phone: data.phone.substr(0, 3) + '****' + data.phone.substr(7),
                      registType: data.registType
                    })
                  }
                }
              })
            }
          })
        }
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
              console.log("getMineInfo :")
              console.log(data)
              if (data.phone != null && data.phone.length > 0){
                that.setData({
                  phone: data.phone.substr(0, 3) + '****' + data.phone.substr(7),
                  registType: data.registType
                })
              }  
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