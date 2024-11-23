const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
const { User, VerificationCode } = require('../models');
const dotenv = require('dotenv');
const moment = require('moment-timezone');

dotenv.config();

function generateVerificationCode() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

function generateRandomPassword() {
    return Math.random().toString(36).slice(-8);
}

function getCurrentTimeInIstanbul() {
    return moment().tz('Europe/Istanbul').format('DD/MM/YYYY HH:mm:ss');
}

exports.login = async (req, res) => {
    const { email, password } = req.body;

    try {
        const user = await User.findOne({ where: { email } });
        if (!user) {
            return res.status(404).json({ message: 'Kullanıcı bulunamadı', timestamp: getCurrentTimeInIstanbul() });
        }

        const isPasswordValid = await bcrypt.compare(password, user.password);
        if (!isPasswordValid) {
            return res.status(401).json({ message: 'Geçersiz şifre', timestamp: getCurrentTimeInIstanbul() });
        }

        const token = jwt.sign({ id: user.id, role: user.role }, process.env.JWT_SECRET, {
            expiresIn: '1h' // Token süresi 1 saat.
        });

        res.json({ token, role: user.role, timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Sunucu hatası', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.forgotPassword = async (req, res) => {
    const { email } = req.body;

    try {
        const user = await User.findOne({ where: { email } });
        if (!user) {
            return res.status(404).json({ message: 'E-posta bulunamadı', timestamp: getCurrentTimeInIstanbul() });
        }

        const verificationCode = generateVerificationCode();
        const expiration = new Date();
        expiration.setMinutes(expiration.getMinutes() + 2);

        await VerificationCode.upsert({ email, code: verificationCode, expiration });

        const transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS
            }
        });

        const formattedVerificationCode = verificationCode.split('').join(' ');
        const mailOptions = {
            from: process.env.EMAIL_USER,
            to: email,
            subject: 'Otomasyon Şifre Sıfırlama Kodu',
            html: `
                <div style="font-family: 'Roboto', Arial, sans-serif; line-height: 1.6; color: #333; padding: 20px; background-color: #f9f9f9; border-radius: 10px; max-width: 600px; margin: auto; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <h2 style="color: #000000; font-size: 28px; margin: 0;">Teknik Bilimler MYO Otomasyon Sistemi</h2>
                    </div>
                    <p style="font-size: 18px; margin: 0 0 10px;">Sayın Yetkili,</p>
                    <p style="font-size: 18px; margin: 0 0 20px;">Şifre sıfırlama işlemi için doğrulama kodunuz aşağıda belirtilmiştir:</p>
                    <div style="text-align: center; margin: 20px 0;">
                        <span style="font-size: 30px; font-weight: bold; color: #ffffff; border-radius: 10px; padding: 16px 27px; background-color: #000AFF; text-decoration: none; display: inline-block;">${formattedVerificationCode}</span>
                    </div>
                    <p style="font-size: 18px; margin: 0 0 20px;">Bu kod 2 dakika boyunca geçerlidir.</p>
                    <p style="font-size: 18px; margin: 0;">Saygılarımla,</p>
                    <p style="font-size: 18px; margin: 10px 0 0;">Teknik Bilimler MYO Otomasyon Sistemi Yapımcısı<strong style="font-size: 18px; font-weight: bold; color:#000000; display: inline-block; margin-left: 5px;">Mehmet Altınkılıç</strong></p>
                </div>
            `
        };

        transporter.sendMail(mailOptions, (error, info) => {
            if (error) {
                return res.status(500).json({ message: 'E-posta gönderim hatası', error: error.message, timestamp: getCurrentTimeInIstanbul() });
            } else {
                res.json({ message: 'Doğrulama kodu e-postanıza gönderildi', timestamp: getCurrentTimeInIstanbul() });
            }
        });
    } catch (err) {
        res.status(500).json({ message: 'Sunucu hatası', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.verifyCode = async (req, res) => {
    const { email, code } = req.body;

    try {
        const verification = await VerificationCode.findOne({ where: { email, code } });
        if (!verification) {
            return res.status(400).json({ message: 'Geçersiz veya süresi dolmuş doğrulama kodu', timestamp: getCurrentTimeInIstanbul() });
        }

        if (new Date() > verification.expiration) {
            return res.status(400).json({ message: 'Doğrulama kodunun süresi dolmuş', timestamp: getCurrentTimeInIstanbul() });
        }

        res.json({ message: 'Doğrulama kodu geçerli', timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Sunucu hatası', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

function isValidPassword(password) {
    const minLength = 8;
    const hasNumber = /\d/;
    const hasLetter = /[a-zA-Z]/;
    return password.length >= minLength && hasNumber.test(password) && hasLetter.test(password);
}

exports.resetPassword = async (req, res) => {
    const { email, code, newPassword } = req.body;

    try {
        if (!isValidPassword(newPassword)) {
            return res.status(400).json({ message: 'Şifre en az 8 karakter uzunluğunda olmalı, en az 1 sayı ve en az 1 harf içermelidir.', timestamp: getCurrentTimeInIstanbul() });
        }

        const verification = await VerificationCode.findOne({ where: { email, code } });
        if (!verification) {
            return res.status(400).json({ message: 'Geçersiz veya süresi dolmuş doğrulama kodu', timestamp: getCurrentTimeInIstanbul() });
        }

        if (new Date() > verification.expiration) {
            return res.status(400).json({ message: 'Doğrulama kodunun süresi dolmuş', timestamp: getCurrentTimeInIstanbul() });
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        await User.update({ password: hashedPassword }, { where: { email } });
        await VerificationCode.destroy({ where: { email, code } });

        res.json({ message: 'Şifre başarıyla sıfırlandı', timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Sunucu hatası', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.addUser = async (req, res) => {
    const { email } = req.body;

    if (!email) {
        return res.status(400).json({ message: 'E-posta gereklidir.', timestamp: getCurrentTimeInIstanbul() });
    }

    try {
        const existingUser = await User.findOne({ where: { email } });
        if (existingUser) {
            return res.status(400).json({ message: 'Kullanıcı zaten mevcut', timestamp: getCurrentTimeInIstanbul() });
        }

        const randomPassword = generateRandomPassword();
        const hashedPassword = await bcrypt.hash(randomPassword, 10);

        const user = await User.create({ email, password: hashedPassword });

        // Yeni şifreyi kişinin E-postasına gönderimini simüle ettim şimdilik
        console.log(`E-posta gönderildi: ${email}`);
        console.log(`Geçici şifre: ${randomPassword}`);

        // Kullanıcı başarıyla eklendi mesajını burada gönderiyoruz
        res.json({ message: 'Kullanıcı başarıyla eklendi.', user, timestamp: getCurrentTimeInIstanbul() });

    } catch (err) {
        res.status(500).json({ message: 'Kullanıcı eklenemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};


exports.listUsers = async (req, res) => {
    try {
        const users = await User.findAll();
        res.json({ users, timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Kullanıcılar getirilemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.deleteUser = async (req, res) => {
    const { id } = req.params;
    try {
        const user = await User.findByPk(id);
        if (!user) {
            return res.status(404).json({ message: 'Kullanıcı bulunamadı', timestamp: getCurrentTimeInIstanbul() });
        }

        await User.destroy({ where: { id } });
        res.json({ message: 'Kullanıcı başarıyla silindi', timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Kullanıcı silinemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

// kullanıcıları listeleme fonk
exports.listUsers = async (req, res) => {
    try {
        const users = await User.findAll();
        res.json(users); 
    } catch (err) {
        res.status(500).json({ message: 'Kullanıcılar getirilemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};
