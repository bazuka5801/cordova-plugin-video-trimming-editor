import Cordova
import Cordova
import Foundation
import AVFoundation
import PryntTrimmerView

@objc(VideoTrimmingEditor) class VideoTrimmingEditor : CDVPlugin {
    
    @objc func open(_ command: CDVInvokedUrlCommand) {
        let params = command.arguments[0] as AnyObject
        
        var sessionId = "id"
        if let _sessionId: String = params["id"] as? String {
            sessionId = _sessionId
        }
        
        guard let inputPath = params["input_path"] as? String else {
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Parameter Error")
            self.commandDelegate.send(result, callbackId:command.callbackId)
            return
        }
        
        var maxDuration = 30
        if let _maxDuration = params["video_max_time"] as? Int {
            maxDuration = _maxDuration
        }
        
        let viewController = VideoTrimmingEditorViewController(inputPath, maxDuration: maxDuration)
        
        viewController.startCallback = {
        }

        viewController.successCallback = { (arg) in
            let (videoPath, imagePath) = arg
            let data = ["output_path": videoPath, "thumbnail_path": imagePath]
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:data as [AnyHashable : Any])
            self.commandDelegate.send(result, callbackId:command.callbackId)
        }
        
        viewController.errorCallback = {
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR)
            self.commandDelegate.send(result, callbackId:command.callbackId)
        }
        
        viewController.progressCallback = { (progress) in
            self.commandDelegate.evalJs("cordova.fireDocumentEvent('VideoTrimming.exportProgress', {'progress':\(progress),'id':'\(sessionId)'});")
        }
        self.viewController.present(viewController, animated: true, completion: nil)
    }
}
