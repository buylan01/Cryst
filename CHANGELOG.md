# Changelog

## 1.0.0 (2026-06-27)


### Features

* Add AboutLibrary Library ([42a0fa2](https://github.com/buylan01/Cryst/commit/42a0fa2008330bf73b95a13dbe2024dbb900eacf))
* add clickable time property row with date picker ([457f7b6](https://github.com/buylan01/Cryst/commit/457f7b6dc28fc6b80237a26f60b7654c523810ca))
* Add FileTypeRegistry, "heif, apex" is available ([1a9edc6](https://github.com/buylan01/Cryst/commit/1a9edc6262c6e00a073f61b0721d1f22f1ceef9e))
* **apk:** add signature verification with apksig ([13e486b](https://github.com/buylan01/Cryst/commit/13e486bbdbaea3387388055ea9ad657c4976e426))
* ApkInfo Dialog Unpack Button Available ([3dce729](https://github.com/buylan01/Cryst/commit/3dce72963013233faacdf3eba02c3e32f28071d8))
* Compress File, with bug fixes ([7c964ba](https://github.com/buylan01/Cryst/commit/7c964ba2f6c602d3609624f829ad69fb3ee8fd9b))
* Copy from archive is available ([111a6c5](https://github.com/buylan01/Cryst/commit/111a6c5d2b444c612b62f89f9736192766d4e6c8))
* **home:** add haptic feedback on swipe threshold ([71ff8f9](https://github.com/buylan01/Cryst/commit/71ff8f9318b9ace10543a9c58e13e6c5508cc374))
* Picture thumbnail ([a7b6191](https://github.com/buylan01/Cryst/commit/a7b6191073778652573d320f78923940b8e1a468))
* Scroll Bar in home ([8e51d84](https://github.com/buylan01/Cryst/commit/8e51d84e5fbc3520cd60ea639779b32bb463a356))
* Sort apps in AppsActivity ([d1282bc](https://github.com/buylan01/Cryst/commit/d1282bc39e003581626578ac23ccd63fe0aa727d))
* TextEditor can redo/undo and save ([1e3c85f](https://github.com/buylan01/Cryst/commit/1e3c85f8e26a0c1dad7abc4983c15ce0146fc484))
* un(re)Navigate path, fix icon ([3911a82](https://github.com/buylan01/Cryst/commit/3911a82e00cc9e458239353f1ab87dac65b42708))
* **vfs:** add NativeFile implementation for shell-based file operations ([ba05782](https://github.com/buylan01/Cryst/commit/ba057824e67e43228198c5a498c712db9acbb773))


### Bug Fixes

* **Animate in Panel:** 由于破AnimatedContent出现了一些bug, 导致LazyColumn 的items测量出现问题，只能使用animateItem临时代替 ([7a2ad0c](https://github.com/buylan01/Cryst/commit/7a2ad0cdfafbbd39c947ee9b05821db103b549e3))
* **apk verification:** correct v1 detection and lower min platform ([574e874](https://github.com/buylan01/Cryst/commit/574e8740506efff4baf42dc842ee166aaead18e6))
* Auto Dark Mode in Text Editor ([e51c751](https://github.com/buylan01/Cryst/commit/e51c751cdebe7f08acb5718dde40d34f33d0c76b))
* **deps:** sora-editor to 0.24.6 ([24d7d78](https://github.com/buylan01/Cryst/commit/24d7d78f9711b0240e22f17f83b0e7a016f29aac))
* **dialog:** handle null archive with dismiss and toast ([6faa537](https://github.com/buylan01/Cryst/commit/6faa537b9f5efef150c2ee5114cfc1a341b9f043))
* failed to build release ([a4718a7](https://github.com/buylan01/Cryst/commit/a4718a7f90c514e45f2fe3c120ddd8446584f1df))
* Finish viewModel when dismiss copy dialog ([d28453e](https://github.com/buylan01/Cryst/commit/d28453e31c56e813ff6d054fa565e8b43536feb2))
* **home:** add scroll to drawer and limit its width ([3da7723](https://github.com/buylan01/Cryst/commit/3da772362140eb78798d77ce9fdc41d38b60cb4f))
* IndexOutOfBoundsException in clear path history ([1c6f1a0](https://github.com/buylan01/Cryst/commit/1c6f1a0c590f6023bd1428f79b0cdbbd41f54b86))
* Low Quality in PictureViewer ([d714aa5](https://github.com/buylan01/Cryst/commit/d714aa5136de799f743ad9efede674dc31481a3d))
* refresh when Delete file ([667280a](https://github.com/buylan01/Cryst/commit/667280ac9548ca215b0392bc3dcd334a62832357))
* Resolution issue in imageViewer ([a3c36c7](https://github.com/buylan01/Cryst/commit/a3c36c7ed290d3db84ed0b997ef2873400688852))
* search in archive ([ccf3557](https://github.com/buylan01/Cryst/commit/ccf355778e1369240eb75a92614c5876524b8c62))
* tar compressor cannot put a single dir. Panel didn't refresh after create. ui: improve create ui ([1dab39b](https://github.com/buylan01/Cryst/commit/1dab39bf998156c2bd913a1181a4564b46b7b7b0))
* Tools may not enable currently in ToolDialog ([6b3f64d](https://github.com/buylan01/Cryst/commit/6b3f64dfbd3941b7e4586e0dd400fec722f21f80))
* use displayCutout for content window insets in screens ([1fd5b1f](https://github.com/buylan01/Cryst/commit/1fd5b1faf6a9dd48ac20f6b9c0981f13ae3ea186))
* **vfs:** rename fail, and much improvement ([0f7cda8](https://github.com/buylan01/Cryst/commit/0f7cda86f8971aaefe909a4386febc0292088708))
* **vfs:** resolve OOM and excessive memory usage in ArchiveFile ([c733b90](https://github.com/buylan01/Cryst/commit/c733b900429dc7b71ff1a41c7bb0ef04169a3e84))
