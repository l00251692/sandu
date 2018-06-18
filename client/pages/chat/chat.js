import { getMessage, sendMsg } from '../../utils/api'

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
    emojis: []
  },


  onLoad(options) {
    this.fromid = options.fromid

    var that = this
    this.initEmoji()
    
    wx.getSystemInfo({
      success: (res) => {
        this.setData({
          windowHeight: res.windowHeight,
          pxToRpx: 750 / res.screenWidth,
          scrollHeight: (res.windowHeight - 50) * 750 / res.screenWidth
        })

        that.initData()
        that.loadMsg() 
      }
    })
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

  initData() {
    this.setData(initData)
  },

  loadMsg(cb) {
    var that = this

    var fromid = this.fromid
    var { page } = this.data

    getMessage({
      page,
      fromid,
      success(data) {
        console.log("getMessage:" + JSON.stringify(data))
        var { messages } = that.data
        var { list, count, page } = data
        messages = messages ? messages.concat(list) : list
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
          toView: "ID_9"
        })
      },
      error(data) {

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
    this.setData({
      isShow: !this.data.isShow,
      isLoad: false,
      cfBg: !this.data.false
    })
  },
  //表情选择
  emojiChoose: function (e) {
    //当前输入内容和表情合并
    this.setData({
      msg: this.data.msg + e.currentTarget.dataset.emoji
    })
  },
  //点击emoji背景遮罩隐藏emoji盒子
  cemojiCfBg: function () {
    this.setData({
      isShow: false,
      cfBg: false
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
    if (that.data.msg.trim().length > 0) {
      sendMsg({
        to_id: this.fromid,
        msg: that.data.msg,
        success(data) {
          that.setData({
            msg: "",//清空文本域值
            isShow: false,
            cfBg: false,
            scrollTop: that.data.scrollTop + 1000,
          })
          that.initData()
          that.loadMsg()
          that.setData({
            toView: "ID_9"
          })
        },
        error(data) {
          console.log("提交评论失败，请稍后")
        }
      })

    } else {
      that.setData({
        msg: ""//清空文本域值
      })
    }
  }
  
})

