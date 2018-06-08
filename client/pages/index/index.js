import {
  getBannerInfo,getProjectList
} from '../../utils/api'
import {
  alert
} from '../../utils/util'

Page({
  data: {
    page: 0,
    hasMore: true,
    loading: false
  },
    onLoad: function () {
      var that = this
      this.init()    
    },
    onShow: function () {
      // 页面显示
    },
    search: function (e) {

        wx.navigateTo({
            url: '/pages/activity/search/index'
        });

    },
    init()
    {
      var that = this
      this.initData()
      this.getBanner()
      this.getProjectList()
    },
    initData() {
      this.setData({
        page: 0,
        hasMore: true,
        loading: false,
        projectList: null
      })
    },
    getBanner:function(e) {
      if (this.data.loading) {
        return;
      }
      var that = this;
      getBannerInfo(
      {
          success(data) {
            that.setData({
              banner_arr: JSON.parse(data)
            })
          }
      })      
    },
    getProjectList: function (e) { 
      if (this.data.loading) {
        return;
      }
      var that = this;
      var { page } = this.data
      getProjectList(
      {
          page,
          success(data) {
            var list  = data.list
            var { projectList} = that.data
            that.setData({
              projectList: projectList ? projectList.concat(list) : list,
              page: page + 1,
              hasMore: data.count == 10, //一次最多10个，如果这次取到10个说明还有
              loading: false
            })
          }
      })
    },
    create_project:function(e)
    {
      getApp().getLoginInfo(loginInfo => {
        if (loginInfo != null && loginInfo.is_login){
          wx.navigateTo({
            url: '/pages/project/create'
          });
        }
        else
        {
          alert("用户未授权，请先在'我的'页面登录")
        }
         
      })
    },
    onReachBottom(e) {
       if (this.data.hasMore && !this.data.loading) {
         this.getProjectList();
       }
    },
    callback() {
       this.init()
    },
})
