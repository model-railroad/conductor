package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class MapInfo {
    private String mName
    private String mSvg

    MapInfo() {
    }

    MapInfo(@NonNull String name,
            @NonNull String svg) {
        mName = name
        mSvg = svg
    }

    @NonNull
    String getName() {
        return mName;
    }

    void setName(@NonNull String name) {
        mName = name;
    }

    @NonNull
    String getSvg() {
        return mSvg;
    }

    void setSvg(@NonNull String svg) throws IOException {
        mSvg = svg;

//        if (svg.startsWith("@")) {
//            mSvg = mFileOps.toString(new File(svg.substring(1)), Charsets.UTF_8);
//        } else {
//        mSvg = svg;
//        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MapInfo mapInfo = (MapInfo) o

        if (mName != mapInfo.mName) return false
        if (mSvg != mapInfo.mSvg) return false

        return true
    }

    int hashCode() {
        int result
        result = mName.hashCode()
        result = 31 * result + mSvg.hashCode()
        return result
    }
}
