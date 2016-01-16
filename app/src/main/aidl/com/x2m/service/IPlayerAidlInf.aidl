// IPlayerAidlInf.aidl
package com.x2m.service;

// Declare any non-default types here with import statements

interface IPlayerAidlInf {
    void play(String filePath);
    void stop();
    void pause();
    void seek(int offset);
}
