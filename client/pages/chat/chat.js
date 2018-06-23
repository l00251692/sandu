import { getMessage, sendMsg, setMsgRead } from '../../utils/api'

import { connectWebsocket, getPrevPage } from '../../utils/util'

var initData = {
  page: 0,
  hasMore: true,
  messages: [],
  loading: false,
}

Page({
  data: {
    isSpeech: false,
    scrollHeight: 0,
    toView: '',
    windowHeight: 0,
    windowWidth: 0,
    pxToRpx: 2,
    msg: '',
    cfBg: false,
    emojiChar: "😃-😋-😌-😍-😏-😜-😝-😞-😔-😪-😭-😁-😂-😃-😅-😆-👿-😒-😓-😔-😏-😖-😘-😚-😒-😡-😢-😣-😤-😢-😨-😳-😵-😷-😸-😻-😼-😽-😾-😿-🙊-🙋-🙏-✈-🚇-🚃-🚌-🍄-🍅-🍆-🍇-🍈-🍉-🍑-🍒-🍓-🐔-🐶-🐷-👦-👧-👱-👩-👰-👨-👲-👳-💃-💄-💅-💆-💇-🌹-💑-💓-💘-🚲",
    //0x1f---
    emoji: [
      "60a", "60b", "60c", "60d", "60f",
      "61b", "61d", "61e", "61f",
      "62a", "62c", "62e",
      "602", "603", "605", "606", "608",
      "612", "613", "614", "615", "616", "618", "619", "620", "621", "623", "624", "625", "627", "629", "633", "635", "637",
      "63a", "63b", "63c", "63d", "63e", "63f",
      "64a", "64b", "64f", "681",
      "68a", "68b", "68c",
      "344", "345", "346", "347", "348", "349", "351", "352", "353",
      "414", "415", "416",
      "466", "467", "468", "469", "470", "471", "472", "473",
      "483", "484", "485", "486", "487", "490", "491", "493", "498", "6b4"
    ],
    emojis: [],
    socketMsgQueue: []
  },


  onLoad(options) {
    this.fromid = options.fromid
    this.callback = options.callback || 'callback'

    var that = this
    this.initEmoji()

    wx.getSystemInfo({
      success: (res) => {
        this.setData({
          windowHeight: res.windowHeight,
          pxToRpx: 750 / res.screenWidth,
          scrollHeight: (res.windowHeight - 50) * 750 / res.screenWidth
        })

        var websocketFlag = wx.getStorageSync('websocketFlag')
        var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

        connectWebsocket({
          user_id,
          success(data) {
            
          },
          error(){

          }
        })
        that.initConnectWebSocket()
        that.initData()
        that.loadMsg()
        that.setMsgRead()
      }
    })
  },

  onUnload(){
    this.setMsgRead()
  },

  initEmoji() {
    var that = this

    var emojis = []
    var em = {}
    var emChar = that.data.emojiChar.split("-");
    that.data.emoji.forEach(function (v, i) {
      em = {
        char: emChar[i],
        emoji: "0x1f" + v
      };
      emojis.push(em)
    });

    that.setData({
      emojis: emojis
    })
  },

  initConnectWebSocket() {

    var that = this
    wx.onSocketOpen(function (res) {
      console.log('WebSocket连接已打开！')
      wx.setStorageSync('websocketFlag', true)

      var { socketMsgQueue } = that.data

      for (var i = 0; i < socketMsgQueue.length; i++) {
        sendSocketMessage(JSON.stringify(socketMsgQueue[i]))
      }

      that.setData({
        socketMsgQueue: []
      })
    })

    wx.onSocketError(function (res) {
      console.log('WebSocket连接打开失败，请检查！')
      wx.setStorageSync('websocketFlag', false)
    })

    wx.onSocketMessage(function (res) {

      var  tmp = JSON.parse(res.data)
      var { user_id } = getApp().globalData.loginInfo.userInfo

      if (tmp.type == "userMsg" && tmp.toId == user_id) {
    
        var { messages } = that.data
        var msgTmp = { id: tmp.id, time: tmp.time, img: tmp.fromHeadUrl, text: tmp.msg }
        messages.push(msgTmp)

        that.setData({
          messages: messages
        })

        that.setData({
          toView: tmp.id
        })
      }
    })
  },

  initData() {
    this.setData(initData)
  },

  loadMsg(cb) {
    var that = this
    var { hasMore, page, loading } = this.data

    if (loading) {
      return
    }

    this.setData({
      loading: true
    })

    var fromid = this.fromid

    if (hasMore) {
      getMessage({
        page,
        fromid,
        success(data) {
          var { messages } = that.data
          var { list, count, page, newId } = data
          messages = messages ? list.concat(messages) : list
          that.setData({
            messages,
            title: data.concat_name,
            hasMore: count == 10,
            loading: false,
            page: page + 1
          })

          wx.setNavigationBarTitle({
            title: that.data.title
          })

          that.setData({
            toView: newId
          })
        },
        error(data) {

        }
      })
    }


  },

  setMsgRead(cb){
    var concat_id = this.fromid

    setMsgRead({
      concat_id,
      success(data){
        //
      }
    })
  },

  textAreaFocus: function () {
    this.setData({
      isShow: false,
      cfBg: false
    })
  },
  //点击表情显示隐藏表情盒子
  emojiShowHide: function () {
    if(!this.data.isShow)
    {
      //设置消息scroll-view高度检出评论高度50再减去表情列表的view的高度200
      this.setData({
        isShow: !this.data.isShow,
        isLoad: false,
        cfBg: !this.data.false,
        scrollHeight: (this.data.windowHeight -200 - 50) * this.data.pxToRpx
      })
    }
    else{
      this.setData({
        isShow: !this.data.isShow,
        isLoad: false,
        cfBg: !this.data.false,
        scrollHeight: (this.data.windowHeight - 50) * this.data.pxToRpx
      })
    }
  },
  //表情选择
  emojiChoose: function (e) {
    //当前输入内容和表情合并
    this.setData({
      msg: this.data.msg + e.currentTarget.dataset.emoji,
    })
    
  },
  //点击emoji背景遮罩隐藏emoji盒子
  cemojiCfBg: function () {
    this.setData({
      isShow: false,
      cfBg: false,
      scrollHeight: (this.data.windowHeight - 50) * this.data.pxToRpx
    })
  },

  onMsgInput(e) {
    var { value: msg } = e.detail
    this.setData({
      msg
    })
  },

  onShareAppMessage: function () {
    return {
      title: '伙伴小Q',
      path: '/pages/index/index'
    }
  },

  send: function () {
    var that = this
    var concatId = this.fromid
    var { socketMsgQueue, messages } = this.data
    var websocketFlag = wx.getStorageSync('websocketFlag')
    var { user_id, avatarUrl } = getApp().globalData.loginInfo.userInfo
    var time = Date.parse(new Date())

    if(this.data.msg == null || this.data.msg.length == 0)
    {
      wx.showToast({
        title: '发送消息为空',
      })
      return
    }

    var id = 'id_' + time / 1000;
    var msgTmp = { id: id, time: time, me: user_id, img: avatarUrl, text: that.data.msg, to: concatId}

    messages.push(msgTmp)

    if (websocketFlag) {
      wx.sendSocketMessage({
        data: JSON.stringify(msgTmp),
        success(res){
          console.log("send ok," + JSON.stringify(res))
        },
        fail(res){
          console.log("send fail," + JSON.stringify(res))
        }
      })
    } else {
      socketMsgQueue.push(msg)
    }

    that.setData({
      msg: "",//清空文本域值
      isShow: false,
      cfBg: false,
      messages: messages,
      scrollHeight: (this.data.windowHeight - 50) * this.data.pxToRpx
    })

    that.setData({
      toView: id
    })
  },

  scrollUpper(e) {
    this.loadMsg()
  }
})

