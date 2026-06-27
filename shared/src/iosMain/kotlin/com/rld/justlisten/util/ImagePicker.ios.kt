@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
package com.rld.justlisten.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import kotlinx.cinterop.*

class ImagePickerDelegate(
    private val onImagePicked: (String) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    private var selfReference: ImagePickerDelegate? = this

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            
        if (image != null) {
            val data = UIImageJPEGRepresentation(image, 0.8)
            if (data != null) {
                val paths = NSSearchPathForDirectoriesInDomains(
                    NSDocumentDirectory,
                    NSUserDomainMask,
                    true
                )
                val documentsDirectory = paths.firstOrNull() as? String
                if (documentsDirectory != null) {
                    val fileName = "picked_profile_${NSUUID.UUID().UUIDString()}.jpg"
                    val filePath = "$documentsDirectory/$fileName"
                    if (data.writeToFile(filePath, true)) {
                        onImagePicked(filePath)
                    }
                }
            }
        }
        picker.dismissViewControllerAnimated(true) {
            selfReference = null
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            selfReference = null
        }
    }
}

class IosImagePickerLauncher(
    private val onImagePicked: (String) -> Unit
) : ImagePickerLauncher {
    override fun launch() {
        val rootController = UIApplication.sharedApplication.connectedScenes
            .mapNotNull { it as? UIWindowScene }
            .flatMap { it.windows }
            .mapNotNull { it as? UIWindow }
            .firstOrNull { it.isKeyWindow() }
            ?.rootViewController
            
        if (rootController != null) {
            val picker = UIImagePickerController()
            picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            val delegate = ImagePickerDelegate(onImagePicked)
            picker.delegate = delegate
            rootController.presentViewController(picker, true, null)
        }
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher {
    return remember {
        IosImagePickerLauncher(onImagePicked)
    }
}
