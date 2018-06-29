
var host = "ailogic.xin"
//var host = "127.0.0.1"
const debug = wx.getStorageSync('debug')
if (debug) {
  host = "localhost"
}

module.exports = {
  host,
  qqmapKey: 'FPOBZ-UT2K2-ZFYUC-CX67E-IOOYS-7XFQ6'
}
