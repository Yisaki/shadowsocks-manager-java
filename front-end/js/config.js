//自定义初始化
var apiURLPrefix='http://localhost:5668';
var staticURLPrefix='http://localhost/static';

layui.data("chaos:login",{ key: 'url' ,value: apiURLPrefix});
layui.data("chaos:login",{ key: 'staticUrl' ,value: staticURLPrefix});
layui.config({
    base: staticURLPrefix+'/js/modules/'      //自定义layui组件的目录
}).extend({ //设定组件别名
    common:   'common',
});

