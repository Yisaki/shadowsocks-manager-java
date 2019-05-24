//自定义初始化
var apiURLPrefix='http://localhost:5668';
var staticURLPrefix='http://localhost/static';

layui.sessionData("chaos:login",{ key: 'url' ,value: apiURLPrefix});
layui.sessionData("chaos:login",{ key: 'staticUrl' ,value: staticURLPrefix});
layui.config({
    base: staticURLPrefix+'/js/modules/'      //自定义layui组件的目录
}).extend({ //设定组件别名
    common:   'common',
});

