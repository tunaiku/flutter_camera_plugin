import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';

class ResultPictureScreen extends StatefulWidget {
  final String imagePath;

  const ResultPictureScreen({
    required this.imagePath,
    Key? key,
  }) : super(key: key);

  @override
  _ResultPictureScreenState createState() => _ResultPictureScreenState();
}

class _ResultPictureScreenState extends State<ResultPictureScreen> {
  String? imageProperties;

  @override
  void initState() {
    super.initState();
    checkImageProperties();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Stack(
        alignment: AlignmentDirectional.bottomEnd,
        children: [
          Container(
            width: MediaQuery.of(context).size.width,
            height: MediaQuery.of(context).size.height,
            child: Image.file(
              File(widget.imagePath),
            ),
          ),
          Container(
            color: Colors.white,
            width: MediaQuery.of(context).size.width,
            height: 50,
            child: Text(
              imageProperties ?? "",
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
    );
  }

  checkImageProperties() async {
    File imageFile = File(widget.imagePath);
    final imageBytes = await imageFile.readAsBytes();
    final bytes = imageBytes.lengthInBytes;
    final kiloBytes = bytes / 1024;
    final megaBytes = kiloBytes / 1024;
    Size imageSize = await _calculateImageDimension(imageFile);
    setState(() {
      imageProperties =
          "Size Image: $megaBytes MB, Image Resolution : $imageSize";
    });
  }

  Future<Size> _calculateImageDimension(File imageFile) {
    Completer<Size> completer = Completer();
    Image image = Image.file(imageFile);
    image.image.resolve(ImageConfiguration()).addListener(
      ImageStreamListener(
        (ImageInfo image, bool synchronousCall) {
          var myImage = image.image;
          Size size = Size(myImage.width.toDouble(), myImage.height.toDouble());
          completer.complete(size);
        },
      ),
    );
    return completer.future;
  }
}
