var app = getApp()
import { getMessageList } from '../../utils/api'

import { connectWebsocket } from '../../utils/util'

var initData = {
  page: 0,
  hasMore: true,
  list: null,
  modalHidden: true,
  loading: false,
  toast1Hidden: true
}

Page({
    data:{

    },
    onLoad: function () {

      if (!getApp().globalData.loginInfo.is_login) {
        return 
      }

      var websocketFlag = wx.getStorageSync('websocketFlag')
      var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

      connectWebsocket({
        user_id,
        success(data){
          
        },
        error(){

        }
      })  
      this.initConnectWebSocket()
      this.initData()
    },

    onReady:function(){
    },

    initConnectWebSocket(){
      var that = this
      wx.onSocketOpen(function (res) {
        console.log('WebSocket连接已打开！')
        wx.setStorageSync('websocketFlag', true)
      })

      wx.onSocketError(function (res) {
        console.log('WebSocket连接打开失败，请检查！')
        wx.setStorageSync('websocketFlag', false)
      })

      wx.onSocketMessage(function (res) {
        console.log("收到socket 信息")
        if (res.data == '连接成功') {
          console.log('连接成功')
        }
        else if (res.data == '新订单') {
          that.loadData()
        }
      })
    },

    initData(cb) {
      this.setData(initData)
      this.loadData(cb)
    },

    loadData(cb) {
      var that = this

      var { page, loading} = this.data

      if (loading) {
        return
      }

      this.setData({
        loading: true,
      })

      getMessageList({
        page,
        success(data){
          var { list } = that.data
          var { list2, count, page } = data

          that.setData({
            list: list ? list.concat(list2) : list2,
            loading:false,
            hasMore: count == 10,
            page: page + 1
          })

        },
        error(data){
          console.log("读取消息列表失败")
        }
      })

    },

    onReachBottom(e) {
      this.initData()
    },

    goPage:function(e){
        console.log(e)
        var that = this;
        var newlist = that.data.list
        var index = e.currentTarget.dataset.index
        newlist[index].count=0;
        console.log("gppage:" + e.currentTarget.dataset.id)
        that.setData({
            list: newlist
        })
        
        wx.navigateTo({
          url: '../chat/chat?fromid='+e.currentTarget.dataset.id
        })
    },

    toast1Change:function(){
        this.setData({
            toast1Hidden: true
        })
    },
    onPullDownRefresh:function(){
        
        util.getUser(this);
        
    }
})
