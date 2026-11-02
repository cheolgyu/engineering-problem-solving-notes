            common_file="$common_dir/$proto_rel"
            android_file="$android_dir/$proto_rel"
            if [ ! -f "$android_file" ]; then
              echo "::error file=$common_file::android 사본 없음: $android_file"
              drift_found=1
              continue
            fi
            if ! diff -q "$common_file" "$android_file" > /dev/null; then
              echo "::error file=$common_file::android 사본과 drift: $android_file"
              diff "$common_file" "$android_file" | head -20
              drift_found=1
            fi
          done < <(cd "$common_dir" && find . -name "*.proto" | sed 's|^\./||')

          if [ $drift_found -eq 1 ]; then
            echo ""
            echo "::error::proto drift — ./gradlew :core:copyProtos 실행 후 결과를 commit 하세요."
            exit 1
          fi
          echo "✓ android proto 사본이 common/proto/v1 과 동일"
