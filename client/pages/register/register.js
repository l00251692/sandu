// login.js
import { alert, getPrevPage } from '../../utils/util'
import { registerSan } from '../../utils/api'

Page({
  data: {
    loading:false,
    color_index: 0,
    color: ['红色', '橙色', '黄色', '绿色', '青色', '蓝色', '紫色', '灰色', '粉色', '黑色', '白色', '棕色', '混合色'],
    style_index: 0,
    style: ['三折伞', '五折伞', '长柄伞'],
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.callback = options.callback || 'callback'
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
  },
  
  bindPickerColor: function (e) {
    console.log('picker发送选择改变，携带值为', e.detail.value)
    this.setData({
      color_index: e.detail.value
    })
  },

  bindPickerStyle: function (e) {
    console.log('picker发送选择改变，携带值为', e.detail.value)
    this.setData({
      style_index: e.detail.value
    })
  },

  bindFeatureInput(e) {
    this.setData({
      feature: e.detail.value
    })
  },

  registerSubmit(e) {
    var that = this
    var { loading, color, color_index, style, style_index, feature } = this.data

    if (loading) {
      return;
    }
    
    this.setData({
      loading: true
    })

    if(feature == null)
    {
      feature = "no"
    }

    registerSan({
      color_reg:color[color_index],
      style_reg:style[style_index],
      feature_reg:feature,
      success(data) {
        that.setData({
          loading: false
        })
        wx.showToast({
          title: '绑定信息成功',
          icon: 'success',
          duration: 1000
        })
        setTimeout(function () {
          wx.navigateTo({
            url: '/pages/service/service?registType=2',
          })
        }, 1000)
      },
      error() {
        that.setData({
          loading: false
        })
        wx.showToast({
          title: '绑定信息失败',
          icon: 'fail',
          duration: 2000
        })
      }
    })

  }
})