import { ORDER_STATES } from './../order/constant'
import {
  getDriverOrders
} from '../../utils/api'

import {
  datetimeFormat,alert
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
      if (this.ballance > 0){
        alert("受不可描述原因限制，提现暂未开放，请联系管理员提现")
      }
      else{
        alert("余额大于0才可以提现")
      }
    }

});

