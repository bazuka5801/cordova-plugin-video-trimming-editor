//
//  UIColor+Hex.swift
//  CordovaPluginsStatic
//
//  Created by Максим Базуев on 10/12/23.
//

import Foundation

extension UIColor {
    convenience init?(hex: String) {
        let r, g, b, a: CGFloat

        var hexNumber: UInt64 = 0
        
        var hexS = hex
        if hex.hasPrefix("#") {
            let start = hex.index(hex.startIndex, offsetBy: 1)
            hexS = String(hex[start...])
        }
        let scanner = Scanner(string: hexS)

        if scanner.scanHexInt64(&hexNumber) {
            if hexS.count == 6 {
                r = CGFloat((hexNumber & 0xFF0000) >> 16) / 255
                g = CGFloat((hexNumber & 0x00FF00) >> 8) / 255
                b = CGFloat(hexNumber & 0x0000FF) / 255
                a = 1.0
            } else if hexS.count == 8 {
                r = CGFloat((hexNumber & 0xFF000000) >> 24) / 255
                g = CGFloat((hexNumber & 0x00FF0000) >> 16) / 255
                b = CGFloat((hexNumber & 0x0000FF00) >> 8) / 255
                a = CGFloat(hexNumber & 0x000000FF) / 255
            } else {
                return nil
            }
            
            self.init(red: r, green: g, blue: b, alpha: a)
            return
        }
        return nil
    }
}
