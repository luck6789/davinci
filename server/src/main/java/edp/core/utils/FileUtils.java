/*
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2018 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * >>
 */

package edp.core.utils;

import com.alibaba.druid.util.StringUtils;
import edp.core.consts.Consts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUtils {


    @Value("${file.userfiles-path}")
    public String fileBasePath;


    /**
     * 校验MultipartFile 是否图片
     *
     * @param file
     * @return
     */
    public boolean isImage(MultipartFile file) {

        Pattern pattern = Pattern.compile(Consts.REG_IMG_FORMAT);
        Matcher matcher = pattern.matcher(file.getOriginalFilename());

        return matcher.find();
    }

    public boolean isImage(File file) {

        Pattern pattern = Pattern.compile(Consts.REG_IMG_FORMAT);
        Matcher matcher = pattern.matcher(file.getName());

        return matcher.find();
    }

    /**
     * 校验MultipartFile 是否csv文件
     *
     * @param file
     * @return
     */
    public boolean isCsv(MultipartFile file) {
        return file.getOriginalFilename().endsWith(".csv");
    }

    /**
     * 上传文件
     *
     * @param file
     * @param path     上传路径
     * @param fileName 文件名（不含文件类型）
     * @return
     * @throws IOException
     */
    public String upload(MultipartFile file, String path, String fileName) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String format = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String newFilename = fileName + "." + format;

        String returnPath = (path.endsWith("/") ? path : path + "/") + newFilename;

        String filePath = fileBasePath + returnPath;

        File dest = new File(filePath);

        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
        }

        file.transferTo(dest);

        return returnPath;
    }


    /**
     * 下载文件
     *
     * @param filePath
     * @param response
     */
    public void download(String filePath, HttpServletResponse response) {
        if (!StringUtils.isEmpty(filePath)) {
            File file = null;
            if (!filePath.startsWith(fileBasePath)) {
                file = new File(fileBasePath + filePath);
            }else {
                file = new File(filePath);
            }
            if (file.exists()) {
                byte[] buffer = new byte[0];
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(filePath));
                    buffer = new byte[is.available()];
                    is.read(buffer);
                    response.reset();
                    response.addHeader("Content-Disposition", "attachment;filename=" + new String(file.getName().getBytes(), "UTF-8"));
                    response.addHeader("Content-Length", "" + file.length());
                    os = new BufferedOutputStream(response.getOutputStream());
                    response.setContentType("application/octet-stream;charset=UTF-8");
                    os.write(buffer);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != is) {
                            is.close();
                        }
                        if (null != os) {
                            os.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    remove(filePath);
                }
            }
        }
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return
     */
    public boolean remove(String filePath) {
        if (!filePath.startsWith(fileBasePath)) {
            filePath = fileBasePath + filePath;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
            return true;
        }
        return false;
    }


    /**
     * 删除文件夹及其下文件
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 压缩文件到zip
     * @param files
     * @param targetFile
     */
    public static void zipFile(List<File> files, File targetFile) {
        byte[] bytes = new byte[1024];

        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile));
            for (File file : files) {
                FileInputStream in = new FileInputStream(file);
                out.putNextEntry(new ZipEntry(file.getName()));
                int length;
                while ((length = in.read(bytes)) > 0) {
                    out.write(bytes, 0, length);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
