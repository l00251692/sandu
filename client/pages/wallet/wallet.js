import { ORDER_STATES } from './../order/constant'
import {
  getDriverOrders, getBalanceToWx
} from '../../utils/api'

import {
  datetimeFormat, alert, confirm
} from '../../utils/util'


var initData = {
  page: 0,
  hasMore: true,
  loading: false,
  list: null
}


Page({
    data: {
      ORDER_STATES
    },
    onLoad: function (optisons) {
      this.ballance = optisons.ballance
      this.initData()
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

    initData(cb) {
      this.setData(initData)
      this.loadData(cb)
    },

    loadData(cb) {
      var that = this
      var {
        loading, page
      } = this.data

      if (loading) {
        return
      }

      this.setData({
        loading: true,
        ballance: this.ballance
      })

      getDriverOrders({
        page,
        success(data) {
          var { list } = that.data
          var { list2, count, page } = data
          console.log("myorderlist:" + JSON.stringify(data))
          list2 = list2.map(item => {
            item['create_time_format'] = datetimeFormat(item.create_time)
            return item
          })
          that.setData({
            loading: false,
            list: list ? list.concat(list2) : list2,
            hasMore: count == 5,
            page: page + 1
          })
          console.log("get list  ok")
          cb && cb()

        },
        error(data) {
          that.setData({
            loading: false
          })
        }
      })
    },

    onReachBottom(e) {
      this.loadData()
    },

    onPullDownRefresh() {
      console.log("onPullDownRefresh")
      wx.showNavigationBarLoading()
      this.initData(() => {
        wx.hideNavigationBarLoading()
        wx.stopPullDownRefresh()
      })
    },

    onBalanceBack(){

      confirm({
        content: '受微信限制暂不支持提现到零钱，请联系客服留言提现',
        cancelText: '取消',
        confirmText: '确定',
        ok() {
          wx.showToast({
            title: '谢谢您的理解',
          })
        }
      })

    //   var balance = this.ballance

    //   console.log("tixianjie:" + balance)

    //   if (parseFloat(balance) > 0.0){
    //     confirm({
    //       content: '将提现所有余额到微信零钱',
    //       cancelText: '取消',
    //       confirmText: '确定',
    //       ok() {
    //         getBalanceToWx({
    //           money: balance,
    //           success(data){
    //             wx.showToast({
    //               title: '提现申请成功',
    //             })
    //           },
    //           error(data){}
    //         })
    //         wx.showToast({
    //           title: '提现申请失败，请联系客服',
    //         })
    //       }
    //     })
    //   }
    //   else{
    //     alert("余额大于0才可以提现")
    //   }
     }

});

