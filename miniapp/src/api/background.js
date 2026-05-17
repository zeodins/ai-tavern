import request from './request'

const BASE_URL = 'http://localhost:8080'

export function getBackgroundUrl(backgroundId) {
    if (!backgroundId) return ''
    return BASE_URL + '/api/backgrounds/' + backgroundId
}

export function uploadBackground(characterId, filePath) {
    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: BASE_URL + '/api/characters/' + characterId + '/background',
            filePath,
            name: 'file',
            success: (res) => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    resolve(JSON.parse(res.data))
                } else {
                    reject(new Error(res.data))
                }
            },
            fail: (err) => reject(err)
        })
    })
}
