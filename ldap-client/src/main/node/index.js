const ldap = require('ldapjs')
const fs = require('fs')

const client = ldap.createClient({
    url: 'ldap://127.0.0.1:389'
});

const entry = {
    name: 'cn=root',
    pwd: '12345678'
}


let buf = new Buffer(2048);
const certUrl = 'C:\\Users\\cnh\\Desktop\\蔡贤勇_加密证书.cer'

new Promise((resolve, reject) => {
    client.bind(entry.name, entry.pwd, (err, data) => {
        err ? reject(err) : resolve(data)
    })
}).then(data => {

    console.log('bind success')
    return new Promise((resolve, reject) => {
        fs.open(certUrl, 'r+', function (err, fd) {
            if (err) return console.error(err);
            console.log('准备读取文件');

            fs.read(fd, buf, 0, buf.length, 0, function (err, bytes) {
                if (err) reject(err);
                console.log(bytes + ' 字节被读取');
                if (bytes > 0) {
                    resolve(buf.slice(0, bytes))
                }
            })
        })
    })
}).then(data => new Promise((resolve, reject) => {

    console.log(data)
    const attr = new ldap.Attribute({
        id:'fdsf',
        type: 'usercert;binary'
    })
    attr.addValue(data)

    const change = new ldap.Change({
        operation: 'add',
        modification: attr
    });

    client.modify(entry.name, change, (err, data) => {
        err ? reject(err) : resolve(data)
    })
})).then(data => {
    console.log(data)
}).catch(err => {
    console.log(err)
})
