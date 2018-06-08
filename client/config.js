// var host = "apitest.ipaotui.com"
//var host = "api.ipaotui.com"
var host = "localhost"
const debug = wx.getStorageSync('debug')
if (debug) {
  host = "localhost"
}


module.exports = {
  host,
  qqmapKey: 'FPOBZ-UT2K2-ZFYUC-CX67E-IOOYS-7XFQ6'
}
