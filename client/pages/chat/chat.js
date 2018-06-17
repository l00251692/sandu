
import { getMessage } from '../../utils/api'
Page({
  data: {
    text: "这是消息页面，研发中。。。",
    title: "标题",
    userInfo: {},
    message: [],
    animation: {},
    animation_2: {},
    tap: "tapOff",
    disabled: true,
    content: "",
    cfBg: false,
    _index: 0,
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
    emojis: [] //qq、微信原始表情

  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.init()
    this.fromid = options.fromid
  },
  onReady: function () {
    // 页面渲染完成
    var that = this
    wx.setNavigationBarTitle({
      title: that.data.title
    })
    this.animation = wx.createAnimation();
    this.animation_2 = wx.createAnimation()
  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏
  },
  onUnload: function () {
    // 页面关闭
  },

  init(cb) {
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

    getMessage({
      fromid: this.fromid,
      success(data) {
        that.setData({
          title: data.from_name,
          message:data.message
        })
      },
      error(data) {

      }
    })

  },


  //文本域获得焦点事件处理
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
      content: this.data.content + e.currentTarget.dataset.emoji
    })
  },
  //点击emoji背景遮罩隐藏emoji盒子
  cemojiCfBg: function () {
    this.setData({
      isShow: false,
      cfBg: false
    })
  },

  onCommentInput(e) {
    var { value: content } = e.detail
    this.setData({
      content
    })
  },
  //发送评论评论 事件处理
  send: function () {
    var that = this
    if (that.data.content.trim().length > 0) {
      sendProjdectComment({
        project_id: this.id,
        comment: that.data.content,
        success(data) {
          that.setData({
            content: "",//清空文本域值
            isShow: false,
            cfBg: false
          })
          that.initComment()
          that.loadReview()
          getPrevPage()[that.callback]()
        },
        error(data) {
          alert("提交评论失败，请稍后")
        }
      })

    } else {
      that.setData({
        content: ""//清空文本域值
      })
    }
  }
})