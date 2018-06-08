import {
  fetch, coordFormat,
  alert, confirm,
} from './util'

// 获取商店列表
export function getSellers(options) {
  var {
    page,
    success
  } = options
  page = page || 0
  getApp().getCurrentAddress(address => {
    var location = address.location
    fetch({
      url: 'campus/getAllCampusWx',
      data: {
        page,
        city_name: address.city,
        city_id: address.city_id,
        district_name: address.district,
        district_id: address.district_id,
        longitude: location.longitude,
        latitude: location.latitude
      },
      success
    })
  })
}

// 获取商店详情
export function getSellerInfo(options) {
  var {
    seller_id,
    success, complete
  } = options
  getApp().getCurrentAddress(address => {
    var location = address.location
    fetch({
      url: 'campus/getCampusByIdWx',
      data: {
        seller_id,
        longitude: location.longitude,
        latitude: location.latitude
      },
      success, complete
    })
  })
}

// 获取商店评论
export function getReviews(options) {
  var {
    seller_id, page,
    success
  } = options
  page = page || 0
  fetch({
    url: 'campus/getReviews',
    data: {
      seller_id, page
    },
    success
  })
}
/*
// 短信验证码
export function getCode(options) {
  const {
    phone, success, error
  } = options
  fetch({
    url: "index.php?m=Api&c=Common&a=checkMSG",
    data: {
      phone,
      key: 'fast_login'
    },
    success, error
  })
}

// 登录
export function login(options) {
  const {
    phone, code,
    success, error
  } = options
  wx.login({
    success(res) {
      getApp().getCurrentAddress(address => {
        fetch({
          url: 'index.php?m=Api&c=WeixinMall&a=login',
          data: {
            phone, code,
            wx_code: res['code'],
            session_3rd: wx.getStorageSync('session_3rd'),
            city_id: address.city_id,
            city_name: address.city,
            district_id: address.district_id,
            district_name: address.district,
          },
          success, error
        })
      })
    },
    error(res) {
      alert(res['errMsg'])
      error && error(res['errMsg'])
    }
  })

}
// 退出账号
export function logout(options) {
  const {
    phone,
    success, error
  } = options
  fetch({
    url: 'index.php?m=Api&c=WeixinMall&a=logout',
    data: {
      phone
    },
    success, error
  })
}
*/

// 获取登录信息
export function getLoginInfo(options) {
  const {
    success, error, fail
  } = options

  //调用登录接口
  wx.login({
    success(res) {
      wx.getUserInfo({
        success: function (userRes) {
          fetch({
            url: 'user/toLoginWx',
            data: {
              wx_code: res.code,
              encryptedData: userRes.encryptedData,
              iv: userRes.iv
            },
            success,
            error
          })
        },
        fail
      })
    },
    error(res) {
      alert(res['errMsg'])
      error && error(res['errMsg'])
    }

  })
}

export function getMineInfo(options) {
  const {
    success
  } = options
  if (!getApp().globalData.loginInfo.is_login) {
    return alert('用户未登录')
  }
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'user/getMineInfoWx',
    data: {
      user_id,
    },
    success
  })
}


// 获取用户地址列表
export function getUserAddrs(options) {
  const {
    success, error
  } = options

  if (!getApp().globalData.loginInfo.is_login) {
    return alert('用户未登录')
  }
  var { user_id } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'receiver/getUserAddrsWx',
    data: {
      user_id
    },
    success, error
  })


}
// 获取用户地址
export function getUserAddr(options) {
  const {
    addr_id,
    success, error
  } = options

  if (!getApp().globalData.loginInfo.is_login) {
    return alert('用户未登录')
  }

  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'receiver/getUserAddrWx',
    data: {
      user_id,
      addr_id
    },
    success, error
  })
}

// 新增用户地址
export function addUserAddr(options) {
  if (options.addr_id) {
    return updateUserAddr(options)
  }
  const {
    receiver, phone, detail, address,
    success, error
  } = options
  
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  var gps = address.gps
  if (!gps) {
    var location = coordFormat(address.location)
    gps = `${location.longitude},${location.latitude}`
  }
  fetch({
    url: 'receiver/addUserAddrWx',
    data: {
      user_id,
      receiver, phone, detail,
      gps,
      addr: address.title,
      city_id: address.city_id,
      city_name: address.city,
      district_id: address.district_id,
      district_name: address.district,
    },
    success, error
  })
}

// 修改地址
export function updateUserAddr(options) {
  const {
    receiver, phone, detail, address,
    addr_id,
    success, error
  } = options

    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
    var gps = address.gps
    if (!gps) {
      var location = coordFormat(address.location)
      gps = `${location.longitude},${location.latitude}`
    }
    fetch({
      url: 'receiver/updateUserAddrWx',
      data: {
        user_id, user_token,
        receiver, phone, detail,
        gps, addr_id,
        addr: address.title,
        city_id: address.city_id,
        city_name: address.city,
        district_id: address.district_id,
        district_name: address.district,
      },
      success, error
    })
}

// 删除地址
export function deleteUserAddr(options) {
  const {
    addr_id,
    success, error
  } = options

  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'receiver/deleteUserAddrWx',
    data: {
      user_id,
      addr_id
    },
    success, error
  })
}

 //添加准订单
export function addQuasiOrder(options) {
  const {
    seller_id,
    totalPackingFee,
    goods,
    success, error
  } = options

  var data = {
    seller_id,
    totalPackingFee,
    goods: JSON.stringify(goods)
  }
 
  if(!getApp().globalData.loginInfo.is_login){
    //用户先登录
    getApp().getLoginInfo(loginInfo=>{
      var { user_id, user_token } = loginInfo.userInfo
      fetch({
        url: 'order/createOrderWx',
        data: Object.assign({
          user_id,
        }, data),
        success, error
      })
    })
  } 
  else
  {
    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
    fetch({
      url: 'order/createOrderWx',
      data: Object.assign({
        user_id,
      }, data),
      success, error
    })
  } 
}

// 获取准订单
export function getQuasiOrderInfo(options) {
  var {
    quasi_order_id,
    success, error
  } = options

  if (!getApp().globalData.loginInfo.is_login) {
    return alert('用户未登录')
  }
  console.log("quasi_order_id:"+quasi_order_id)
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/getQuasiOrderInfoWx',
    data: {
      user_id, user_token,
      quasi_order_id
    },
    success, error
  })
}

// 更新准订单地址
export function updateOrderAddr(options) {
  var {
    quasi_order_id, addr_id,
    success, error
  } = options
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/updateOrderAddrWx',
    data: {
      user_id,
      quasi_order_id, addr_id
    },
    success, error
  })

}
/*
// 更新准订单红包
export function updateOrderCoupon(options) {
  var {
    quasi_order_id, user_coupon_id,
    success, error
  } = options
  getApp().getLoginInfo(loginInfo => {
    if (!loginInfo.user_info) {
      return alert('用户未登录')
    }
    var { user_id, user_token } = loginInfo.user_info
    fetch({
      url: 'index.php?m=Mall&c=Order&a=updateOrderCoupon',
      data: {
        user_id, user_token,
        quasi_order_id, user_coupon_id
      },
      success, error
    })

  })
}
*/
// 添加订单
export function addOrder(options) {
  var {
    quasi_order_id, remark, addr_id,
    success, error
  } = options
  
  if (!getApp().globalData.loginInfo.is_login)
  {
    return alert('用户未登录')
  }
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/submitOrder',
    data: {
      user_id, 
      quasi_order_id, addr_id,
      remark
    },
    success, error
  })

}

// 取消订单
export function cancelOrder(options) {
  var {
    order_id,
    success, error
  } = options
  
  if (!getApp().globalData.loginInfo.is_login) {
    return alert('用户未登录')
  }
  
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/cancelOrderWx',
    data: {
      user_id,
      order_id
    },
    success, error
  })
}

// 获取订单列表
export function getOrders(options) {
  var {
    page,
    success, error
  } = options

  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/getOrdersMineWx',
    data: {
      user_id,
      page
    },
    success, error
  })

}

export function getShopOrders(options) {
  var {
    page,campus_id,
    success, error
  } = options
  console.log("getShopOrders:" + page)
  fetch({
    url: 'order/getShopOrdersWx',
    data: {
      campus_id,
      page
    },
    success, error
  })

}

// 获取订单详情
export function getOrderInfo(options) {
  var {
    order_id,
    success, error
  } = options
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/getOrdersInfoWx',
    data: {
      user_id,
      order_id
    },
    success, error
  })

}

// 订单评论
export function reviewsOrder(options) {
  var {
    order_id,
    service, quality, content,
    success, error
  } = options
  console.log("11")
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'service/creatOrderCommentWx',
    data: {
      user_id,
      order_id,
      service, quality, content,
    },
    success, error
  })

}

// 获取支付参数
export function getPayment(options) {
  var {
    order_id,
    pay_money,
    success, error
  } = options
  if (!getApp().globalData.loginInfo.is_login) {
    return alert('用户未登录')
  }
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'pay/getPaymentWx',
    data: {
      order_id,
      user_id,
      pay_money
    },
    success, error
  })

}

export function paySuccess(options) {
  var {
    order_id, prepay_id, success
  } = options
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'pay/setPaySuccessWx',
    data: {
      order_id,
      user_id,
      prepay_id
    },
    success
  })

}

export function setRecvOrder(options) {
  var {
    order_id,
    success, error
  } = options

  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/setRecvOrderWx',
    data: {
      order_id,
      user_id
    },
    success, error
  })

}

export function setRejectOrder(options) {
  var {
    order_id,
    success, error
  } = options

  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'order/setRejectOrderWx',
    data: {
      order_id,
      user_id
    },
    success, error
  })

}

/*
// 获取分组列表
export function getSellersByCategory(options) {
  var {
    category_id, page,
    success, error
  } = options
  page = page || 0
  getApp().getCurrentAddress(address => {
    var {
      location,
      city_id,
      city: city_name,
      district_id,
      district: district_name
    } = address
    fetch({
      url: 'index.php?m=Mall&c=Seller&a=getSellersByCategory',
      data: {
        category_id,
        city_id, city_name,
        district_id, district_name,
        page,
        gps: `${location.longitude},${location.latitude}`,
      },
      success, error
    })

  })
}

// 搜索商家和商品
export function search(options) {
  var {
    keyword, page,
    success, error
  } = options
  page = page || 0
  getApp().getCurrentAddress(address => {
    var {
      location: { longitude, latitude },
      city_id,
      city: city_name,
      district_id,
      district: district_name
    } = address
    fetch({
      url: 'index.php?m=Mall&c=Seller&a=search',
      data: {
        keyword,
        city_id, city_name,
        district_id, district_name,
        page,
        longitude, latitude
      },
      success, error
    })

  })
}


// 获取用户红包列表
export function getShareUserList(options) {
  var {
    page,
    success, error
  } = options
  page = page || 0
  getApp().getLoginInfo(loginInfo => {
    if (!loginInfo.user_info) {
      return alert('用户未登录')
    }
    var { user_id, user_token } = loginInfo.user_info
    fetch({
      url: 'index.php?m=Mall&c=Coupon&a=getShareUserList',
      data: {
        user_id, user_token,
        page
      },
      success, error
    })

  })
}
*/


// 获取banner信息
export function getBannerInfo(options) {
  var {
    success
  } = options

  fetch({
    url: 'project/getBannerInfoWx',
    data: {},
    success
  })

}
//获取发布的项目列表
export function getProjectList(options) {
  var {
    page,success
  } = options
  
  fetch({
    url: 'project/getProjectListWx',
    data: {
      page
    },
    success
  })

}

export function getMyProjectList(options) {
  var {
    page, success
  } = options
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'project/getMyProjectListWx',
    data: {
      user_id,
      page
    },
    success
  })

}
//获得项目详情
export function getProjectInfo(options) {
  var {
    project_id, success
  } = options
  fetch({
    url: 'project/getProjectInfoWx',
    data: {
      project_id,
    },
    success
  })

}

//获取发布的项目列表
export function createProject(options) {
  var {
    title, concat, instruction, success
  } = options
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'project/createProjectWx',
    data: {
      user_id,
      title,
      concat,
      instruction
    },
    success
  })

}


export function sendProjdectComment(options) {
  var {
    project_id, comment, success, error
  } = options
  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
  fetch({
    url: 'project/sendProjdecCommentWx',
    data: {
      user_id,
      project_id,
      comment
    },
    success,
    error
  })

}


export function getCommentList(options) {
  var {
    project_id, page, success, error
  } = options

  fetch({
    url: 'project/getCommentListWx',
    data: {
      project_id,
      page,
    },
    success,
    error
  })

}

export function getMsgList(options) {

  var {
     success, error
  } = options

  var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

  fetch({
    url: 'user/getMsgListWx',
    data: {
      user_id
    },
    success,
    error
  })
}

export function setProjectCommentRead(options) {
    var { project_id,  success} = options
    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

    fetch({
      url: 'user/setProjectCommentReadWx',
      data: {
        user_id,
        project_id
      },
      success
    })
}