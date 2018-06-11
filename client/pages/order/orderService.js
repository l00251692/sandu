var QQMapWX = require('../../utils/qqmap-wx-jssdk.min.js');
var qqmapsdk;
qqmapsdk = new QQMapWX({
  key:'DHNBZ-2ZLKK-T7IJJ-AXSQW-WX5L6-A6FJZ'
});
import {
  getOrderInfo
} from '../../utils/api'

import { alert } from '../../utils/util'

const app = getApp();
Page({
  data: {
    scale: 14,
    hiddenLoading:false
  },
  onLoad: function (options) {
    this.id = options.id
    var that = this
    let { bluraddress,strLatitude,strLongitude,endLatitude,endLongitude} = app.globalData
    getOrderInfo({
      order_id:that.id,
      success(data){
        that.setData({
          markers: [{
            iconPath: "../../images/assets/str.png",
            id: 0,
            latitude: data.order.strLatitude,
            longitude: data.order.strLongitude,
            width: 30,
            height: 30
          }, {
            iconPath: "../../images/assets/end.png",
            id: 0,
            latitude: data.order.endLatitude,
            longitude: data.order.endLongitude,
            width: 30,
            height: 30
          }],
          polyline: [{
            points: [{
              longitude: data.order.strLongitude,
              latitude: data.order.strLatitude
            }, {
                longitude: data.order.endLongitude,
                latitude: data.order.endLatitude
            }],
            color: "#FF0000DD",
            width: 4,
            dottedLine: true
          }],
          driver:data.driver,
          hiddenLoading:true
        });
      },
      error(data){
        alert("查看详细信息失败，请联系客服")
      }
    })
  },

  onShow(){
    this.mapCtx = wx.createMapContext("didiMap");
    this.movetoPosition();
  },

  onReady(){
   
  },
  movetoPosition: function(){
    this.mapCtx.moveToLocation();
  },
 
  bindregionchange: (e)=>{

    console.log("bindregionchange")

  },

  onScaleSub(e){
    if (this.data.scale > 0)
    {
      this.setData({
        scale: --this.data.scale
      }) 
    }
    
  }, 

  onScalePlus(e) {
    if (this.data.scale < 18)
    {
      this.setData({
        scale: ++this.data.scale
      }) 
    }
  },

  onRevert(e){
    this.mapCtx.moveToLocation();
  },

  //点击merkers  
  bindmarkertap(e) {
    // console.log(e.markerId)
    // wx.showActionSheet({
    //   itemList: ["A"],
    //   success: function (res) {
    //     console.log(res.tapIndex)
    //   },
    //   fail: function (res) {
    //     console.log(res.errMsg)
    //   }
    // })
  }, 

  toCancel(){
    wx.redirectTo({
      url: "/pages/cancel/cancel"
    })
   
  },
  toApp(){
    wx.showToast({
      title: '暂不支持',
      icon: 'success',
      duration: 1000
    })
  },
  toEvaluation(){
    wx.redirectTo({
      url:"/pages/evaluation/evaluation",
    })
  },
  onReady: function () {
    wx.getLocation({
      type: "gcj02",
      success:(res)=>{
        this.setData({
          longitude:res.longitude,
          latitude: res.latitude
        })
      }
      })
     
  },

  
  
})