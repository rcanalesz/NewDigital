//Lo que expone el JS de cordova a la aplicacion que lo va a consumir
var digital = {
    capturar: function (success, error) {
      var options = {};
      cordova.exec(success, error, 'Digital', 'capturar', [options]);
    },
    connect: function (success, error) {
      var options = {};
      cordova.exec(success, error, 'Digital', 'connect', [options]);
    }
  }
  
  //NO TOCAR, SOLO INSTALACION
  cordova.addConstructor(function () {
    if (!window.plugins) {
      window.plugins = {};
    }
  
    window.plugins.digital = digital;
    return window.plugins.digital;
  });