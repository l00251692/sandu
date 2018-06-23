var app = getApp()
import { getMessageList } from '../../utils/api'

import { connectWebsocket } from '../../utils/util'

var initData = {
  page: 0,
  hasMore: true,
  list:[],
  modalHidden: true,
  loading: false,
  toast1Hidden: true,
  msgCount:0
}

Page({
    data:{
      list:[]
    },
    onLoad: function () {

      if (!getApp().globalData.loginInfo.is_login) {
        return 
      }

      var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

      connectWebsocket({
        user_id,
        success(data){
          
        },
        error(){

        }
      })  
      this.initData()
    },

    onShow:function(){

      if (!getApp().globalData.loginInfo.is_login) {
        return
      }
      /*socket 监听要在页面显示的时候初始化否则在离开页面后监听不到 */
      this.initConnectWebSocket()
      
    },

    initConnectWebSocket(){
      var that = this

      console.log("initConnectWebSocket")

      wx.onSocketOpen(function (res) {
        console.log('WebSocket连接已打开！')
        wx.setStorageSync('websocketFlag', true)
      })

      wx.onSocketError(function (res) {
        console.log('WebSocket连接打开失败，请检查！')
        wx.setStorageSync('websocketFlag', false)
      })

      wx.onSocketMessage(function (res) {
        console.log('收到消息onSocketMessage！')
        var tmp = JSON.parse(res.data)
        var { user_id } = getApp().globalData.loginInfo.userInfo

        if (tmp.type == "userMsg" && tmp.toId == user_id) {

          var { list } = that.data
          var countTmp = 0
          for (var i = 0; i < list.length; i++)
          {

            if (list[i].id == tmp.fromId)
            {
              countTmp = list[i].count
              list.splice(i,1)
              break;
            }
          }

          var concatTmp = { id: tmp.fromId ,img: tmp.fromHeadUrl, time: tmp.time, count: (countTmp+1), message: tmp.msg }
          list.unshift(concatTmp)

          that.setData({
            list: list
          })
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
          cb && cb()

        },
        error(data){
          console.log("读取消息列表失败")
          cb && cb()
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
          url: '../chat/chat?callback=callback&&fromid='+e.currentTarget.dataset.id
        })
    },

    toast1Change:function(){
        this.setData({
            toast1Hidden: true
        })
    },
    onPullDownRefresh:function(){  
      if (getApp().globalData.loginInfo.is_login) {
        wx.showNavigationBarLoading()
        this.initData(() => {
          wx.hideNavigationBarLoading()
          wx.stopPullDownRefresh()
        })
      } else {
        wx.stopPullDownRefresh()
      }  
    },

    callback(){
      this.initData()
    },

    send: function () {
      var that = this

      var websocketFlag = wx.getStorageSync('websocketFlag')

      var time = Date.parse(new Date())
      var id = 'id_' + time / 1000;
      var msgTmp = { id: id, time: time, me: 'oJOT00Pt2XSAQFMTykeAPtGvF6QQ', img: 'https://wx.qlogo.cn/mmopen/vi_32/jDib7YJO6TRqZYzvmR5IZbD1zpEpDxUPSzfLLYjubiaiaINia54rgzQOA2p1Viahib2PCCqTUoTCWIYk9JJwuvOJyic6A/132', text: '这是一条测试消息', to: 'oJOT00FckX0Ts9chCtN1KavrZfrA' }


      if (websocketFlag) {
        wx.sendSocketMessage({
          data: JSON.stringify(msgTmp),
          success(res) {
            console.log("send ok," + JSON.stringify(res))
          },
          fail(res) {
            console.log("send fail," + JSON.stringify(res))
          }
        })
      } 
    }
})
