package com.asoss.a3drender.app.GlobalObjects;

public class DataObjects {

        //Sample response
      /*  "score": 3.24533,
                "title": "0_2519_1520.stl",
                "metadata1": "",
                "metadata2": "",
                "size": 582084,
                "zippedSize": 167720
        */

        int Id;
        String score;
        String title;
        String metadata1;
        String metadata2;
        String size;
        String zippedSize;
        String FileLocation;


        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMetadata1() {
            return metadata1;
        }

        public void setMetadata1(String metadata1) {
            this.metadata1 = metadata1;
        }

        public String getMetadata2() {
            return metadata2;
        }

        public void setMetadata2(String metadata2) {
            this.metadata2 = metadata2;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getZippedSize() {
            return zippedSize;
        }

        public void setZippedSize(String zippedSize) {
            this.zippedSize = zippedSize;
        }

        public String getFileLocation() {
            return FileLocation;
        }

        public void setFileLocation(String fileLocation) {
            FileLocation = fileLocation;
        }




}//END
