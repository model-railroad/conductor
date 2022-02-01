package com.alflabs.conductor.v2.script

class MapInfo {
    private String mName
    private String mSvg

    MapInfo() {
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

}
