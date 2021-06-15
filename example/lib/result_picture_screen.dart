import 'dart:io';

import 'package:flutter/material.dart';

class ResultPictureScreen extends StatelessWidget {
  final String imagePath;

  const ResultPictureScreen({
    @required this.imagePath,
    Key key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Container(
          width: MediaQuery.of(context).size.width,
          height: MediaQuery.of(context).size.height,
          child: Image.file(
            File(imagePath),
          ),
        ),
      ),
    );
  }
}
