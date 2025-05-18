package main

import (
	"archive/zip"
	"embed"
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"time"
)

//go:embed resources/Student.jar
var jarFile embed.FS

//go:embed resources/jre.zip
var jreFile embed.FS

var success bool

func main() {
	tmpDir := filepath.Join(os.Getenv("TEMP"), "go-wrapper")
	err := os.MkdirAll(tmpDir, os.ModePerm)
	if err != nil {
		log.Fatalln("创建临时目录失败:", err)
	}

	go jar(tmpDir)
	jre(tmpDir)

	for !success {
		time.Sleep(time.Second)
	}

	runJavaJar(filepath.Join(tmpDir, "Student.jar"), tmpDir)
}

func jre(tmpDir string) {
	data, err := jreFile.ReadFile("resources/jre.zip")
	if err != nil {
		log.Fatalln(err)
	}

	jrePath := filepath.Join(tmpDir, "jre.zip")
	if _, err := os.Stat(jrePath); !os.IsNotExist(err) {
		return
	}

	if err := os.WriteFile(jrePath, data, 0644); err != nil {
		log.Fatalln("写入 JRE 文件失败:", err)
	}

	err = Unzip(jrePath, filepath.Join(tmpDir, "jre"))
	if err != nil {
		log.Fatalln("解压 JRE 文件失败:", err)
	}
}

func jar(tmpDir string) {
	data, err := jarFile.ReadFile("resources/Student.jar")
	if err != nil {
		log.Fatalln("读取嵌入文件失败:", err)
	}

	jarPath := filepath.Join(tmpDir, "Student.jar")
	if err := os.WriteFile(jarPath, data, 0644); err != nil {
		log.Fatalln("写入 JAR 文件失败:", err)
	}

	success = true
}

func Unzip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return err
	}

	defer func(r *zip.ReadCloser) {
		err := r.Close()
		if err != nil {
			log.Printf("关闭 ZIP 文件失败: %v", err)
		}
	}(r)

	dest, err = filepath.Abs(dest)
	if err != nil {
		return err
	}

	for _, f := range r.File {
		fpath := filepath.Join(dest, f.Name)
		if !strings.HasPrefix(fpath, filepath.Clean(dest)+string(os.PathSeparator)) {
			return fmt.Errorf("非法文件路径: %s", f.Name)
		}

		if f.FileInfo().IsDir() {
			if err := os.MkdirAll(fpath, os.ModePerm); err != nil {
				return err
			}
			continue
		}

		if err := os.MkdirAll(filepath.Dir(fpath), os.ModePerm); err != nil {
			return err
		}

		if err := extractFile(f, fpath); err != nil {
			return err
		}
	}

	return nil
}

func extractFile(f *zip.File, destPath string) error {
	rc, err := f.Open()
	if err != nil {
		return err
	}

	defer func(rc io.ReadCloser) {
		err := rc.Close()
		if err != nil {
			log.Printf("关闭文件失败: %v", err)
		}
	}(rc)

	outFile, err := os.OpenFile(destPath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
	if err != nil {
		return err
	}

	defer func(outFile *os.File) {
		err := outFile.Close()
		if err != nil {
			log.Printf("关闭文件失败: %v", err)
		}
	}(outFile)

	_, err = io.Copy(outFile, rc)
	return err
}

func runJavaJar(jarPath, tempDir string) {
	java := func() string {
		if strings.Contains(runtime.GOOS, "win") {
			return "javaw.exe"
		} else {
			return "javaw"
		}
	}()

	command := filepath.Join(tempDir, "jre", "bin", java)
	cmd := exec.Command(command, "-Dio.netty.tryReflectionSetAccessible=true --add-opens java.base/jdk.internal.reflect=ALL-UNNAMED --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/sun.reflect=ALL-UNNAMED", "-jar", jarPath)

	if cmd.Run() != nil {
		_ = cmd.Run()
	}
}
