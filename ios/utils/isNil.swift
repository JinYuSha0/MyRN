//
//  isNil.swift
//  myRN
//
//  Created by Soul on 2021/9/30.
//

extension Optional {
    // 这是对 Optional 的一个扩展
    public struct _MyOptionalNil: ExpressibleByNilLiteral {
        @_transparent
        public init(nilLiteral: ()) {}
    }

    typealias OptionAny = Optional<Any>

    /// 重写一个 === 的操作，来递归判断 Nested Optionals 最深层是否是 nil
    public static func ===(lhs: Wrapped?, rhs: _MyOptionalNil) -> Bool {
        switch lhs {
        case let .some(innerOptionalWrappedValue):
            // 将 some 中的 value 取出，来判断 Any 的情况
            if case let OptionAny.some(innerWrappedValue) = innerOptionalWrappedValue as Optional<Any> {
                // 解包后如果是 .none，则就是 nil
                if case OptionAny.none = innerWrappedValue {
                    return true
                }
                // 如果不是 Optional<Any>.noen 则我们需要递归继续处理更深一层
                return innerWrappedValue === rhs
            }
            return false
        case .none:
            return true
        }
    }
}
