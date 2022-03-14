package com.alflabs.conductor.v2.script

class MapInfo {
    private String mName
    private String mSvg

    MapInfo() {
    }

    MapInfo(String name, String svg) {
        mName = name
        mSvg = svg
    }

    String getName() {
        return mName;
    }

    void setName(String name) {
        mName = name;
    }

    String getSvg() {
        return mSvg;
    }

    void setSvg(String svg) throws IOException {
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
