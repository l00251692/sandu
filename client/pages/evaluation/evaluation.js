const app = getApp()

import {
  getOrderInfo, finishOrderByDriver
} from '../../utils/api'

Page({
  data: {
      star: 0,
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
          passenger: data.passenger,
        });
      }
    })
  },

  toFinish(){

    var that = this
    var order_id = this.id

    console.log("toFinish:" + order_id)
        
    finishOrderByDriver({
      order_id,
      success(data)
      {
        wx.showToast({
          title: '行程已结束',
          success(res){
            setTimeout(() =>{
              wx.switchTab({
                url: '/pages/mine/mine',
              })},2000
            )
          }
        })
      },
      error(data)
      {
        wx.showToast({
          title: '结束行程失败',
        })
      }

    })
  }
});