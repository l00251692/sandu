const app = getApp()

import {
  getOrderInfo, finishOrderByPassenger, getPayment
} from '../../utils/api'

import {
  alert, requestPayment
} from '../../utils/util'

Page({
  data: {
      star: 5,
      starMap: [
         '','','','','',
      ],
      play:'',
      
  },
  myStarChoose(e) {
      let star = parseInt(e.target.dataset.star) || 0;
      this.setData({
          star: star,
      });
  },
  onLoad(options){
    this.id = options.id
    this.loadData()
    
    this.setData({
      play: app.globalData.play
    })
  },

  loadData() {
    var that = this
    var order_id = this.id

    getOrderInfo({
      order_id,
      success(data) {
        that.setData({
          driver: data.driver,
        });
      }
    })
  },

  toFinish(){

    var that = this
    var order_id = this.id
    var { star } = this.data

    console.log("toFinish:" + order_id)

    getPayment({
      order_id,
      pay_money: 1,
      success(data) {
        requestPayment({
          data,
          success() {
            finishOrderByPassenger({
              order_id,
              star,
              prepay_id: data.prepay_id,
              success(data) {
                that.setData({
                  loading: false
                })
                wx.switchTab({
                  url: `/pages/index/index`
                })
              }
            })
          },
        })
      },
      error() {
        that.setData({
          loading: false
        })
      }
    })
  }
});