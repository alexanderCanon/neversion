import React from 'react'

export function AmberBtn({
  children,
  onClick,
  className = '',
  outline = false,
  disabled = false,
  type = 'button',
}: {
  children: React.ReactNode
  onClick?: () => void
  className?: string
  outline?: boolean
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
}) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg font-semibold text-sm transition-all duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed ${
        outline
          ? 'border border-[#F5A623] text-[#F5A623] hover:bg-[#F5A623] hover:text-[#0d0e18]'
          : 'bg-[#F5A623] text-[#0d0e18] hover:bg-[#e09516] active:scale-95'
      } ${className}`}
    >
      {children}
    </button>
  )
}
