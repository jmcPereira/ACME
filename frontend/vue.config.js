module.exports = {
    devServer:{
        port : 8080,
        proxy : {
            '^/api': {
                target: 'http://backend:3000',
                changeOrigin: true,
                secure:false,
                pathRewrite: {'^/api': '/api'}
            },
        }
    }
}