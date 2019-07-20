namespace art {
    namespace mirror {
        class Object{
            // The Class representing the type of the object.
            uint32_t klass_;
            // Monitor and hash code information.
            uint32_t monitor_;

        };
        //简化ArtMethod  只需要关注我们需要的，只需要成员变量声明
        class ArtMethod : public Object {
        public:

            uint32_t access_flags_;
            uint32_t dex_code_item_offset_;
            // Index into method_ids of the dex file associated with this method
            //方法再dex中的索引
            uint32_t method_dex_index_;
            uint32_t dex_method_index_;
            //在方法表的索引
            uint32_t method_index_;
            const void *native_method_;

            const uint16_t *vmap_table_;
            // Short cuts to declaring_class_->dex_cache_ member for fast compiled code access.
            uint32_t dex_cache_resolved_methods_;
            //方法 自发
            // Short cuts to declaring_class_->dex_cache_ member for fast compiled code access.
            uint32_t dex_cache_resolved_types_;


            // Field order required by test "ValidateFieldOrderOfJavaCppUnionClasses".
            // The class we are a part of.
            //所属的函数
            uint32_t declaring_class_;
        };
    }
}
