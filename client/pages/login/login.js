// login.js
import WxValidate from '../../utils/WxValidate'
import Countdown from '../../utils/countdown'
import { alert, getPrevPage } from '../../utils/util'
import { getCode, bindPhone } from '../../utils/api'
var initCount = 60
Page({
  data: {
    codeLabel: '获取验证码',
    phone: '',
    codeNum: ''
  },
  onLoad: function (options) {
    this.phone = options.phone
    // 页面初始化 options为页面跳转所带来的参数
    this.callback = options.callback || 'callback'
    this.countdown = new Countdown(this, 'count')
    this.initValidate()
  },
  onReady: function () {
    // 页面渲染完成
  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏
  },
  onUnload: function () {
    // 页面关闭
    if (this.countdown) {
      this.countdown.stop()
    }
  },
  initValidate() {
    this.validate = new WxValidate({
      phone: {
        required: true,
        tel: true,
      },
      code: {
        required: true,
      },
    }, {
        phone: {
          required: '请输入手机号',
          tel: '请输入有效手机号码'
        },
        code: {
          required: '请输入验证码',
        },
      })
  },
  onInputPhone(e) {
    this.setData({
      phone: e.detail.value
    })
  },
  onGetCode(e) {
    var that = this
    var {phone, count} = this.data
    if (count > 0) {
      return;
    }
    if (!/^1[34578]\d{9}$/.test(phone)) {
      return alert('请输入有效手机号码')
    }
    that.setData({
      count: initCount
    })
    that.countdown.start()
    getCode({
      phone,
      success(data) {
        that.setData({
          codeLabel: '重新获取验证码',
          codeNum: data.codeNumStr
        })
      },
      error() {
        that.countdown.stop()
        that.setData({
          count: 0
        })
      }
    })
  },
  formSubmit(e) {
    var that = this
    var {loading} = this.data
    if(loading) {
      return;
    }
    if (!this.validate.checkForm(e)) {
      const error = this.validate.errorList[0]
      return alert(error.msg)
    }
    this.setData({
      loading: true
    })

    var {phone, code} = e.detail.value
    var { codeNum } = this.data

    if (codeNum == code)
    {
      bindPhone({
        phone,
        success(data) {
          that.setData({
            loading: false
          })
          wx.showToast({
            title: '绑定手机号成功成功',
            icon: 'success',
            duration: 1000
          }) 
          setTimeout(function () {
            getPrevPage()[that.callback]()
            wx.navigateBack()
          }, 1000)
        },
        error() {
          that.setData({
            loading: false
          })
          wx.showToast({
            title: '绑定手机号失败',
            icon: 'fail',
            duration: 2000
          })
        }
      })
    }
    else{
      alert("验证码不正确，请重新输入！")
    }
    
  }
})