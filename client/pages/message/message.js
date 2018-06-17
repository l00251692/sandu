var app = getApp()
import { getMessageList } from '../../utils/api'

var initData = {
  list: null,
  modalHidden: true,
  loading: false,
  toast1Hidden: true
}

Page({
    data:{
      
    },
    onReady:function(){
      this.initData()
    },

    modalChange:function(e){
        this.setData({
            modalHidden: true
        })
    },

    initData(cb) {
      this.setData(initData)
      this.loadData(cb)
    },

    loadData(cb) {
      var that = this

      var { loading} = this.data

      if (loading) {
        return
      }

      this.setData({
        loading: true,
      })

      getMessageList({
        success(data){

          that.setData({
            list:data.list,
            loading:false
          })

        },
        error(data){
          console.log("读取消息列表失败")
        }
      })

    },

    goPage:function(e){
        console.log(e)
        var that = this;
        var newlist = that.data.list
        var index = e.currentTarget.dataset.index
        newlist[index].count=0;
        that.setData({
            list: newlist
        })
        wx.navigateTo({
          url: '../chat/chat?fromid='+e.currentTarget.dataset.id
        })
    },

    toast1Change:function(){
        this.setData({
            toast1Hidden: true
        })
    },
    onPullDownRefresh:function(){
        
        util.getUser(this);
        
    }
})
