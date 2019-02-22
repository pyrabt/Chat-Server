# Chat-Server

## What it does
This is a multithreaded chat server build from the ground up in Java.

## Purpose of this project
Build familiarity and understanding with web design and writing multi-threaded applications.

## The implementation
The server utilizes multi-threading to accomodate numerous user connections to different chat rooms.
This is achieved by creating a new thread for each room.
Message broadcasting and clients joining/leaving is handled via synchronization and selection protocol.

The chat room back-end is written in Javascript, which handles the JSON message data and reactive page elements.

Page styling was implemented with bootstrap.

## Demo
![gif of chat demo](web.gif)
